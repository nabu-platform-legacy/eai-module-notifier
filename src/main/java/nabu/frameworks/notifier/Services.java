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

package nabu.frameworks.notifier;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.Notification;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

@WebService
public class Services {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private ServiceRuntime runtime;
	
	public void notify(
			@WebParam(name = "context") List<String> context, 
			@WebParam(name = "message") String message, 
			@WebParam(name = "description") String description, 
			@WebParam(name = "severity") Severity severity, 
			@WebParam(name = "properties") Object properties,
			@WebParam(name = "type") String type,
			@WebParam(name = "code") String code) {
		
		if (context == null || context.isEmpty()) {
			context = EAIRepositoryUtils.getServiceStack();
			String serviceContext = ServiceUtils.getServiceContext(runtime);
			if (!context.contains(serviceContext)) {
				context.add(serviceContext);
			}
		}
		
		Notification notification = new Notification();
		notification.setContext(context);
		notification.setMessage(message);
		notification.setDescription(description);
		notification.setSeverity(severity == null ? Severity.INFO : severity);
		notification.setProperties(properties);
		notification.setCode(code);
		notification.setType(type);
		
		try {
			EAIResourceRepository.getInstance().getEventDispatcher().fire(notification, this);
		}
		catch (Exception e) {
			logger.error("Could not send out notification", e);
		}
	}
	
}
