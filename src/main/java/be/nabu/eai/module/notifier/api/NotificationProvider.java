package be.nabu.eai.module.notifier.api;


import javax.jws.WebParam;
import javax.validation.constraints.NotNull;

import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public interface NotificationProvider extends Artifact {
	public void notify(@WebParam(name = "context") @NotNull String context, @WebParam(name = "severity") Severity severity, @WebParam(name = "message") String message, @WebParam(name = "description") String description);
}
