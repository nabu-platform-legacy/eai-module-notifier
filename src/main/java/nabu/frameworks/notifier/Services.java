package nabu.frameworks.notifier;

import javax.jws.WebParam;
import javax.jws.WebService;

import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.ServiceUtils;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

@WebService
public class Services {
	
	private ServiceRuntime runtime;
	
	public void notify(
			@WebParam(name = "context") String context, 
			@WebParam(name = "message") String message, 
			@WebParam(name = "description") String description, 
			@WebParam(name = "severity") Severity severity, 
			@WebParam(name = "properties") Object properties) {
		
		if (context == null) {
			context = ServiceUtils.getServiceContext(runtime);
		}
		
		
	}
	
}
