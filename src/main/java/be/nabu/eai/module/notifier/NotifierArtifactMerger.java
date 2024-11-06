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
