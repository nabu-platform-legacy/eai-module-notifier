package be.nabu.eai.module.notifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.InterfaceLister;
import be.nabu.eai.developer.util.InterfaceDescriptionImpl;

public class NotifierInterfaceLister implements InterfaceLister {
	
	private static Collection<InterfaceDescription> descriptions = null;
	
	@Override
	public Collection<InterfaceDescription> getInterfaces() {
		if (descriptions == null) {
			synchronized(NotifierInterfaceLister.class) {
				if (descriptions == null) {
					List<InterfaceDescription> descriptions = new ArrayList<InterfaceDescription>();
					descriptions.add(new InterfaceDescriptionImpl("Notifier", "Notifier Provider", "be.nabu.eai.module.notifier.api.NotificationProvider.notify"));
					NotifierInterfaceLister.descriptions = descriptions;
				}
			}
		}
		return descriptions;
	}

}
