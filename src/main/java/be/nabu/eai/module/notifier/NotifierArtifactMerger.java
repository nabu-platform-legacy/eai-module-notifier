package be.nabu.eai.module.notifier;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactMerger;
import be.nabu.eai.repository.api.Repository;

public class NotifierArtifactMerger implements ArtifactMerger<NotifierArtifact> {

	@Override
	public boolean merge(NotifierArtifact source, NotifierArtifact target, AnchorPane pane, Repository targetRepository) {
		if (target != null) {
			source.mergeConfiguration(target.getConfig(), true);
		}
		new NotifierGUIManager().display(MainController.getInstance(), pane, source);
		return true;
	}

	@Override
	public Class<NotifierArtifact> getArtifactClass() {
		return NotifierArtifact.class;
	}

}
