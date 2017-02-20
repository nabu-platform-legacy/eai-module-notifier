package nabu.frameworks.notifier;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.Notification;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

@WebService
public class Services {
	
	private ServiceRuntime runtime;
	
	public void notify(
			@WebParam(name = "context") List<String> context, 
			@WebParam(name = "message") String message, 
			@WebParam(name = "description") String description, 
			@WebParam(name = "severity") Severity severity, 
			@WebParam(name = "properties") Object properties,
			@WebParam(name = "type") String type,
			@WebParam(name = "code") Integer code) {
		
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
		
		EAIResourceRepository.getInstance().getEventDispatcher().fire(notification, this);
	}
	
}
