package nabu.frameworks.notifier;

import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebService;

import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.Notification;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

@WebService
public class Services {
	
	private ServiceRuntime runtime;
	
	@SuppressWarnings("unchecked")
	public void notify(
			@WebParam(name = "context") List<String> context, 
			@WebParam(name = "message") String message, 
			@WebParam(name = "description") String description, 
			@WebParam(name = "severity") Severity severity, 
			@WebParam(name = "properties") Object properties) {
		
		if (context == null) {
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
		notification.setSeverity(severity);
		
		if (properties != null) {
			if (!(properties instanceof ComplexContent)) {
				properties = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(properties);
			}
			notification.setProperties(properties);
			notification.setType(((ComplexContent) properties).getType() instanceof DefinedType ? ((DefinedType) ((ComplexContent) properties).getType()).getId() : null);
		}
		
		EAIResourceRepository.getInstance().getEventDispatcher().fire(notification, this);
	}
	
}
