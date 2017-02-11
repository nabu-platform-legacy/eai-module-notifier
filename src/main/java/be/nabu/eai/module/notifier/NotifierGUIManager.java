package be.nabu.eai.module.notifier;

import java.io.IOException;
import java.lang.reflect.Method;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.ComplexContentEditor;
import be.nabu.eai.developer.ComplexContentEditor.ValueWrapper;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.managers.base.BaseConfigurationGUIManager;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.NotificationProvider;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.TypeBaseUtils;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class NotifierGUIManager extends BaseJAXBGUIManager<NotifierConfiguration, NotifierArtifact> {

	public NotifierGUIManager() {
		super("Notifier", NotifierArtifact.class, new NotifierManager(), NotifierConfiguration.class);
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
				if (property.getName().equals("context")) {
					instance.getConfig().setContext(value == null ? null : (String) value);
				}
				return super.updateProperty(property, value);
			}
		};
		AnchorPane contextPane = new AnchorPane();
		vbox.getChildren().add(contextPane);
		MainController.getInstance().showProperties(updater, contextPane, false, instance.getRepository(), true);
		
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
		
		SimpleProperty<DefinedService> provider = new SimpleProperty<DefinedService>("provider", DefinedService.class, true);
		BaseConfigurationGUIManager.setInterfaceFilter(provider, "be.nabu.eai.repository.api.NotificationProvider.notify");
		Set<Property<?>> properties = new LinkedHashSet<Property<?>>();
		properties.add(provider);
		
		SimpleProperty<String> type = new SimpleProperty<String>("type", String.class, false);
		SimpleProperty<String> whitelist = new SimpleProperty<String>("whitelist", String.class, false);
		SimpleProperty<String> blacklist = new SimpleProperty<String>("blacklist", String.class, false);
		SimpleProperty<Boolean> isContinue = new SimpleProperty<Boolean>("continue", Boolean.class, true);
		SimpleProperty<Severity> severity = new SimpleProperty<Severity>("severity", Severity.class, false);
		
		properties.add(type);
		properties.add(whitelist);
		properties.add(blacklist);
		properties.add(isContinue);
		properties.add(severity);
		
		AnchorPane propertiesPane = new AnchorPane();
		
		List<Value<?>> values = new ArrayList<Value<?>>();
		values.add(new ValueImpl<DefinedService>(provider, route.getProvider()));
		values.add(new ValueImpl<String>(type, route.getType()));
		values.add(new ValueImpl<String>(whitelist, route.getWhitelist()));
		values.add(new ValueImpl<String>(blacklist, route.getBlacklist()));
		values.add(new ValueImpl<Boolean>(isContinue, route.isContinue()));
		values.add(new ValueImpl<Severity>(severity, route.getSeverity()));
		
		SimplePropertyUpdater simplePropertyUpdater = new SimplePropertyUpdater(true, properties, values.toArray(new Value[values.size()])) {
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				if (property.getName().equals("provider")) {
					route.setProvider((DefinedService) value);
					propertiesPane.getChildren().clear();
					if (value != null) {
//						PropertyUpdater updaterFor = updaterFor((DefinedService) value, route.getProperties());
//						if (updaterFor != null) {
//							MainController.getInstance().showProperties(updaterFor, propertiesPane, true, artifact.getRepository(), true);
//						}
						draw(artifact, route.getProvider(), route.getProperties(), propertiesPane);
					}
				}
				else if (property.getName().equals("whitelist")) {
					route.setWhitelist((String) value);
				}
				else if (property.getName().equals("blacklist")) {
					route.setBlacklist((String) value);
				}
				else if (property.getName().equals("whitelist")) {
					route.setContinue(value == null || !((Boolean) value));
				}
				else if (property.getName().equals("severity")) {
					route.setSeverity((Severity) value);
				}
				else if (property.getName().equals("type")) {
					route.setType((String) value);
				}
				return super.updateProperty(property, value);
			}
		};

		MainController.getInstance().showProperties(simplePropertyUpdater, vbox, false, artifact.getRepository(), true);
		
		// the maincontroller wipes the content of the vbox...
		vbox.getChildren().add(propertiesPane);

		if (route.getProvider() != null) {
			draw(artifact, route.getProvider(), route.getProperties(), propertiesPane);
//			PropertyUpdater updaterFor = updaterFor(route.getProvider(), route.getProperties());
//			if (updaterFor != null) {
//				MainController.getInstance().showProperties(updaterFor, propertiesPane, true, artifact.getRepository(), true);
//			}
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
		Button moveUp = new Button("Move Up");
		moveUp.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				int routeIndex = artifact.getConfig().getRoutes().indexOf(route);
				if (routeIndex > 0) {
					int increment = -1;
					moveRoute(artifact, route, pane, vbox, routeIndex, increment);
				}
			}
		});
		Button moveDown = new Button("Move Down");
		moveDown.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				int routeIndex = artifact.getConfig().getRoutes().indexOf(route);
				if (routeIndex < artifact.getConfig().getRoutes().size() - 1) {
					int increment = 1;
					moveRoute(artifact, route, pane, vbox, routeIndex, increment);
				}
			}
		});
		HBox buttons = new HBox();
		buttons.getChildren().addAll(moveUp, moveDown, delete);
		vbox.getChildren().addAll(buttons, new Separator(Orientation.HORIZONTAL));
	}
	
	private void moveRoute(NotifierArtifact artifact, NotifierRoute route, Pane pane, VBox vbox, int routeIndex, int increment) {
		// move the route
		artifact.getConfig().getRoutes().remove(routeIndex);
		artifact.getConfig().getRoutes().add(routeIndex + increment, route);
		// move the vbox
		int vboxIndex = pane.getChildren().indexOf(vbox);
		pane.getChildren().remove(vboxIndex);
		pane.getChildren().add(vboxIndex + increment, vbox);
		MainController.getInstance().setChanged();
	}
	
	private void draw(NotifierArtifact artifact, DefinedService provider, Map<String, String> map, Pane pane) {
		Method method = EAIRepositoryUtils.getMethod(NotificationProvider.class, "notify");
		List<Element<?>> inputExtensions = EAIRepositoryUtils.getInputExtensions(provider, method);
		
		for (Element<?> element : inputExtensions) {
			if (element.getType() instanceof ComplexType) {
				ComplexContent content = ((ComplexType) element.getType()).newInstance();
				for (String key : map.keySet()) {
					try {
						content.set(key, map.get(key));
					}
					catch (Exception e) {
						// ignore
					}
				}
				Tree<ValueWrapper> tree = new ComplexContentEditor(content, true, artifact.getRepository()) {
					@Override
					public void update() {
						Map<String, String> stringMap = TypeBaseUtils.toStringMap(content);
						map.clear();
						map.putAll(stringMap);
					}
				}.getTree();
				pane.getChildren().clear();
				pane.getChildren().add(tree);
				tree.prefWidthProperty().bind(pane.widthProperty());
				break;
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private PropertyUpdater updaterFor(DefinedService provider, Map<String, String> map) {
		Method method = EAIRepositoryUtils.getMethod(NotificationProvider.class, "notify");
		List<Element<?>> inputExtensions = EAIRepositoryUtils.getInputExtensions(provider, method);
		
		if (inputExtensions.size() > 0) {
			List<Property<?>> properties = BaseConfigurationGUIManager.createProperty(inputExtensions.get(0));
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
		return null;
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
