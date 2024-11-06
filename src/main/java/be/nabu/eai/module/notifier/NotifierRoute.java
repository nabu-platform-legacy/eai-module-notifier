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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.api.InterfaceFilter;
import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.eai.repository.util.KeyValueMapAdapter;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class NotifierRoute {
	
	/**
	 * The provider to use
	 */
	private DefinedService provider;
	
	/**
	 * Whether or not to continue with other routes
	 */
	private boolean isContinue;
	
	/**
	 * The severity you are interested in
	 */
	private Severity severity;
	
	// whitelist: a notification only passes if it matches
	// blacklist: a notification only passes if it doesn't match
	private String whitelist, blacklist;
	
	/**
	 * The type of the notification, if none is given and there are properties, it should use the property type id
	 */
	private String type;
	
	/**
	 * The properties to use for the provider
	 * These can reference the incoming message
	 * For example suppose it is an email provider, you could have a property "subject"
	 * For this property you could fill in the value ="Hello from: " + part/firstName
	 * Where "part" is a structure inside the message object
	 */
	private Map<String, String> properties;

	@InterfaceFilter(implement = "be.nabu.eai.repository.api.NotificationProvider.notify")
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	public DefinedService getProvider() {
		return provider;
	}

	public void setProvider(DefinedService provider) {
		this.provider = provider;
	}

	@XmlJavaTypeAdapter(value = KeyValueMapAdapter.class)
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, String>();
		}
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public boolean isContinue() {
		return isContinue;
	}

	public void setContinue(boolean isContinue) {
		this.isContinue = isContinue;
	}

	public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public String getWhitelist() {
		return whitelist;
	}

	public void setWhitelist(String whitelist) {
		this.whitelist = whitelist;
	}

	public String getBlacklist() {
		return blacklist;
	}

	public void setBlacklist(String blacklist) {
		this.blacklist = blacklist;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
