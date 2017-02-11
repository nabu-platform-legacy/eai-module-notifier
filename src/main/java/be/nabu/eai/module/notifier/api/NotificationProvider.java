package be.nabu.eai.module.notifier.api;

import java.util.List;

import javax.jws.WebParam;
import javax.validation.constraints.NotNull;

import be.nabu.eai.module.notifier.Notification;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;

public interface NotificationProvider extends Artifact {
	public void notify(@WebParam(name = "notification") @NotNull Notification notification);
	public List<Property<?>> getProperties();
}
