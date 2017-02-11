package be.nabu.eai.module.notifier;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class NotifierManager extends JAXBArtifactManager<NotifierConfiguration, NotifierArtifact> {

	public NotifierManager() {
		super(NotifierArtifact.class);
	}

	@Override
	protected NotifierArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new NotifierArtifact(id, container, repository);
	}

}
