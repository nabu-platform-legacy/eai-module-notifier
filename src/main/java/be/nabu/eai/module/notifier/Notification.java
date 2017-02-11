package be.nabu.eai.module.notifier;

import java.util.List;

import be.nabu.libs.types.api.KeyValuePair;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class Notification {
	private String context, message, description;
	private Severity severity;
	private List<KeyValuePair> properties;
	
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Severity getSeverity() {
		return severity;
	}
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
	public List<KeyValuePair> getProperties() {
		return properties;
	}
	public void setProperties(List<KeyValuePair> properties) {
		this.properties = properties;
	}
}
