/*
* Copyright (C) 2017 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.module.notifier;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.Notification;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.artifacts.api.StoppableArtifact;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.evaluator.EvaluationException;
import be.nabu.libs.evaluator.PathAnalyzer;
import be.nabu.libs.evaluator.QueryParser;
import be.nabu.libs.evaluator.types.api.TypeOperation;
import be.nabu.libs.evaluator.types.operations.TypesOperationProvider;
import be.nabu.libs.events.api.EventHandler;
import be.nabu.libs.events.api.EventSubscription;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.ExecutionContext;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.TypeBaseUtils;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class NotifierArtifact extends JAXBArtifact<NotifierConfiguration> implements StartableArtifact, StoppableArtifact {

	private EventSubscription<Notification, Void> subscription;

	private Map<String, TypeOperation> queries = new HashMap<String, TypeOperation>();
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	// originally this was false, broke backwards compatibility because it was very rarely used
	private boolean inheritExecutionContext = true;
	
	// originally this was true, broke backwards compatibility because it was very rarely used and introduced unnecessary (sometimes two-ways) overhead
	private boolean propertiesAsKeyValuePair = false;
	
	public NotifierArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "notifier.xml", NotifierConfiguration.class);
	}

	@Override
	public void stop() throws IOException {
		if (subscription != null) {
			subscription.unsubscribe();
			subscription = null;
		}
	}

	@Override
	public void start() throws IOException {
		if (subscription == null) {
			subscription = getRepository().getEventDispatcher().subscribe(Notification.class, new EventHandler<Notification, Void>() {
				@SuppressWarnings("unchecked")
				@Override
				public Void handle(Notification event) {
					for (NotifierRoute route : getConfig().getRoutes()) {
						try {
							// check that we are interested in the level
							if (route.getSeverity() != null) {
								if (event.getSeverity() == null) {
									continue;
								}
								else if (event.getSeverity().ordinal() < route.getSeverity().ordinal()) {
									continue;
								}
							}
							ComplexContent content = null;
							if (event.getProperties() != null) {
								content = event.getProperties() instanceof ComplexContent ? (ComplexContent) event.getProperties() : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(event.getProperties());
							}
							String type = event.getType();
							if (type == null) {
								type = content == null || !(content.getType() instanceof DefinedType) ? null : ((DefinedType) content.getType()).getId();
							}
							// check if we want a type match
							if (route.getType() != null) {
								if (type == null || (!type.equals(route.getType()) && !type.startsWith(route.getType() + "."))) {
									continue;
								}
							}
							if (route.getWhitelist() != null && !route.getWhitelist().trim().isEmpty()) {
								// if we have a whitelist and no properties, continue
								if (content == null) {
									continue;
								}
								try {
									TypeOperation query = getQuery(route.getWhitelist());
									Object evaluate = query.evaluate(content);
									if (evaluate == null) {
										continue;
									}
									else if (!(evaluate instanceof Boolean)) {
										evaluate = ConverterFactory.getInstance().getConverter().convert(evaluate, Boolean.class);
										if (evaluate == null) {
											throw new EvaluationException("The return value is not boolean");
										}
									}
									// if we didn't pass the whitelist, don't execute
									if (!(Boolean) evaluate) {
										continue;
									}
								}
								catch (ParseException e) {
									logger.error("Could not parse whitelist for " + getId() + ": " + route.getWhitelist(), e);
									continue;
								}
								catch (EvaluationException e) {
									logger.error("Could not evaluate whitelist for " + getId() + ": " + route.getWhitelist(), e);
									continue;
								}
							}
							if (route.getBlacklist() != null && !route.getBlacklist().trim().isEmpty()) {
								// if we have a blacklist and no properties, continue
								if (content == null) {
									continue;
								}
								try {
									TypeOperation query = getQuery(route.getBlacklist());
									Object evaluate = query.evaluate(content);
									if (evaluate == null) {
										continue;
									}
									else if (!(evaluate instanceof Boolean)) {
										evaluate = ConverterFactory.getInstance().getConverter().convert(evaluate, Boolean.class);
										if (evaluate == null) {
											throw new EvaluationException("The return value is not boolean");
										}
									}
									// if we passed the blacklist, don't execute
									if ((Boolean) evaluate) {
										continue;
									}
								}
								catch (ParseException e) {
									logger.error("Could not parse blacklist for " + getId() + ": " + route.getBlacklist(), e);
									continue;
								}
								catch (EvaluationException e) {
									logger.error("Could not evaluate blacklist for " + getId() + ": " + route.getBlacklist(), e);
									continue;
								}
							}
							// if we make it here, execute the service
							DefinedService provider = route.getProvider();
							ComplexContent input = provider.getServiceInterface().getInputDefinition().newInstance();
							
							input.set("identifier", event.getIdentifier());
							input.set("context", event.getContext());
							input.set("severity", event.getSeverity() == null ? Severity.INFO : event.getSeverity());
							input.set("message", event.getMessage());
							input.set("description", event.getDescription());
							input.set("created", event.getCreated());
							input.set("code", event.getCode());
							input.set("type", type);
							input.set("alias", event.getAlias());
							input.set("realm", event.getRealm());
							input.set("deviceId", event.getDeviceId());
							
							// pass in the original properties, can be interesting for generic logging
							if (content != null) {
								input.set("properties", propertiesAsKeyValuePair ? TypeBaseUtils.toProperties(TypeBaseUtils.toStringMap(content)) : event.getProperties());
							}
							
							Map<String, String> map = route.getProperties();
							for (Element<?> element : TypeUtils.getAllChildren(input.getType())) {
								// the interface only has simple types, so any complex type is by definition an extension of the provider
								if (!element.getName().equals("properties") && element.getType() instanceof ComplexType) {
									for (String key : map.keySet()) {
										String value = map.get(key);
										if (value != null) {
											if (value.startsWith("=")) {
												try {
													Object evaluate = getQuery(value.substring(1)).evaluate(content);
													input.set(element.getName() + "/" + key, evaluate);
												}
												catch (Exception e) {
													logger.error("Can not evaluate field " + key + " for service " + provider.getId() + " in notifier " + getId(), e);
													continue;
												}
											}
											else {
												input.set(element.getName() + "/" + key, value);
											}
										}
									}
								}
							}
							ExecutionContext newExecutionContext = null;
							// if we start a new execution context, transactions are scoped to that context
							// in some cases however we want to inherit global transaction management
							// because you can always do custom transactions but not force global ones, we default to inheriting the scope
							// this actually breaks with the past but almost no one is using this atm
							if (inheritExecutionContext) {
								ServiceRuntime runtime = ServiceRuntime.getRuntime();
								if (runtime != null) {
									newExecutionContext = runtime.getExecutionContext();
								}
							}
							if (newExecutionContext == null) {
								newExecutionContext = getRepository().newExecutionContext(SystemPrincipal.ROOT);
							}
							ServiceRuntime runtime = new ServiceRuntime(provider, newExecutionContext);
							try {
								runtime.run(input);
							}
							catch (Exception e) {
								logger.error("Could not execute " + provider.getId() + " for notifier " + getId(), e);
							}
						}
						catch (Exception e) {
							logger.error("Notification route malfunction", e);
						}
						// if we don't want to continue, stop
						if (!route.isContinue()) {
							break;
						}
					}
					return null;
				}
			});
			subscription.filter(new EventHandler<Notification, Boolean>() {
				@Override
				public Boolean handle(Notification event) {
					// we don't care about context, allow it
					if (getConfig().getContext() == null || getConfig().getContext().equals("")) {
						return false;
					}
					// if the service context matches, we are interested
					else if (event.getServiceContext() != null && (event.getServiceContext().equals(getConfig().getContext()) || event.getServiceContext().startsWith(getConfig().getContext() + "."))) {
						return false;
					}
					// if there is no context and we are expecting one, filter it
					else if (event.getContext() == null || event.getContext().isEmpty()) {
						return true;
					}
					// if any of the contexts is equal to or a child of the one we expect, allow it
					else {
						String childContext = getConfig().getContext() + ".";
						for (String context : event.getContext()) {
							if (context.equals(getConfig().getContext()) || context.startsWith(childContext)) {
								return false;
							}
						}
					}
					// filter the rest
					return true;
				}
			});
		}
	}

	@Override
	public boolean isStarted() {
		return subscription != null;
	}
	
	private TypeOperation getQuery(String query) throws ParseException {
		if (!queries.containsKey(query)) {
			synchronized(query) {
				if (!queries.containsKey(query)) {
					queries.put(query, (TypeOperation) new PathAnalyzer<ComplexContent>(new TypesOperationProvider()).analyze(QueryParser.getInstance().parse(query)));
				}
			}
		}
		return queries.get(query);
	}
}
