package be.nabu.eai.module.notifier;

import java.util.LinkedHashMap;
import java.util.Map;

import be.nabu.eai.module.notifier.api.NotificationProvider;

public class NotifierRoute {
	
	/**
	 * The provider to use
	 */
	private NotificationProvider provider;
	
	/**
	 * The properties to use for the provider
	 * These can reference the incoming message
	 * For example suppose it is an email provider, you could have a property "subject"
	 * For this property you could fill in the value ="Hello from: " + part/firstName
	 * Where "part" is a structure inside the message object
	 */
	private Map<String, String> properties;

	public NotificationProvider getProvider() {
		return provider;
	}

	public void setProvider(NotificationProvider provider) {
		this.provider = provider;
	}

	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, String>();
		}
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
}
