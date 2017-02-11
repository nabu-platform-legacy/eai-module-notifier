package be.nabu.eai.module.notifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.module.notifier.api.NotificationProvider;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class NotifierGUIManager extends BaseJAXBGUIManager<NotifierConfiguration, NotifierArtifact> {

	public NotifierGUIManager(String name, Class<NotifierArtifact> artifactClass, ArtifactManager<NotifierArtifact> artifactManager, Class<NotifierConfiguration> configurationClass) {
		super(name, artifactClass, artifactManager, configurationClass);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected NotifierArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new NotifierArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}
	
	public String getCategory() {
		return "Frameworks";
	}
	
	@Override
	protected void display(NotifierArtifact instance, Pane pane) {
		VBox vbox = new VBox();
		super.display(instance, vbox);
		pane.getChildren().add(vbox);
		AnchorPane.setBottomAnchor(vbox, 0d);
		AnchorPane.setTopAnchor(vbox, 0d);
		AnchorPane.setRightAnchor(vbox, 0d);
		AnchorPane.setLeftAnchor(vbox, 0d);
		
		// show the context
		SimpleProperty<String> context = new SimpleProperty<String>("context", String.class, false);
		Set<Property<?>> properties = new LinkedHashSet<Property<?>>();
		properties.add(context);
		SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties, new ValueImpl<String>(context, instance.getConfig().getContext())) {
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				instance.getConfig().setContext(value == null ? null : (String) value);
				return super.updateProperty(property, value);
			}
		};
		AnchorPane contextPane = new AnchorPane();
		MainController.getInstance().showProperties(updater, contextPane, false);
		
		Button add = new Button("Add Route");
		add.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				NotifierRoute route = new NotifierRoute();
				instance.getConfig().getRoutes().add(route);
				draw(instance, route, vbox);
				MainController.getInstance().setChanged();
			}
		});
		vbox.getChildren().addAll(new Separator(Orientation.HORIZONTAL), add, new Separator(Orientation.HORIZONTAL));
		
		// draw existing
		for (NotifierRoute route : instance.getConfig().getRoutes()) {
			draw(instance, route, vbox);
		}
	}

	private void draw(NotifierArtifact artifact, NotifierRoute route, Pane pane) {
		VBox vbox = new VBox();
		pane.getChildren().add(vbox);
		
		Property<NotificationProvider> provider = new SimpleProperty<NotificationProvider>("provider", NotificationProvider.class, true);
		Set<Property<?>> properties = new LinkedHashSet<Property<?>>();
		properties.add(provider);
		
		AnchorPane propertiesPane = new AnchorPane();
		vbox.getChildren().add(propertiesPane);
		
		SimplePropertyUpdater simplePropertyUpdater = new SimplePropertyUpdater(true, properties, new ValueImpl<NotificationProvider>(provider, route.getProvider())) {
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				route.setProvider((NotificationProvider) value);
				propertiesPane.getChildren().clear();
				if (value != null) {
					MainController.getInstance().showProperties(updaterFor((NotificationProvider) value, route.getProperties()), propertiesPane, true);
				}
				return super.updateProperty(property, value);
			}
		};

		MainController.getInstance().showProperties(simplePropertyUpdater, vbox, false);

		if (route.getProvider() != null) {
			MainController.getInstance().showProperties(updaterFor(route.getProvider(), route.getProperties()), propertiesPane, true);
		}
		
		Button delete = new Button("Remove Route");
		delete.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				pane.getChildren().remove(vbox);
				artifact.getConfig().getRoutes().remove(route);
				MainController.getInstance().setChanged();
			}
		});
		vbox.getChildren().addAll(delete, new Separator(Orientation.HORIZONTAL));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private PropertyUpdater updaterFor(NotificationProvider provider, Map<String, String> map) {
		List<Property<?>> properties = provider.getProperties();
		synchronize(map, properties);
		List<Value<?>> values = new ArrayList<Value<?>>();
		for (Property<?> property : properties) {
			String value = map.get(property.getName());
			if (value != null) {
				values.add(new ValueImpl(property, property.getValueClass().equals(String.class) ? value : ConverterFactory.getInstance().getConverter().convert(value, property.getValueClass())));
			}
		}
		return new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(properties), values.toArray(new Value[values.size()])) {
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				map.put(property.getName(), value == null ? null : ConverterFactory.getInstance().getConverter().convert(value, String.class));
				return super.updateProperty(property, value);
			}
		};
	}
	
	private void synchronize(Map<String, String> map, List<Property<?>> properties) {
		List<String> keys = new ArrayList<String>(map.keySet());
		for (Property<?> property : properties) {
			if (!map.containsKey(property.getName())) {
				map.put(property.getName(), null);
			}
			keys.remove(property.getName());
		}
		for (String key : keys) {
			map.remove(key);
		}
	}
}
