package be.nabu.eai.module.notifier;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.resources.api.ResourceContainer;

public class NotifierArtifact extends JAXBArtifact<NotifierConfiguration> {

	public NotifierArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "notifier.xml", NotifierConfiguration.class);
	}

	
	
}
