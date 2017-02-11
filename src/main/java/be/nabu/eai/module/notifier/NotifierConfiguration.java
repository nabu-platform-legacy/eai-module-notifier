package be.nabu.eai.module.notifier;

import java.util.ArrayList;
import java.util.List;

public class NotifierConfiguration {
	
	private String context;
	
	private List<NotifierRoute> routes;

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public List<NotifierRoute> getRoutes() {
		if (routes == null) {
			routes = new ArrayList<NotifierRoute>();
		}
		return routes;
	}

	public void setRoutes(List<NotifierRoute> routes) {
		this.routes = routes;
	}
	
}
