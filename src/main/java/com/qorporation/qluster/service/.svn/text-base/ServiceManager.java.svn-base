package com.qorporation.qluster.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.ClassWalkerFilter;
import com.qorporation.qluster.util.ErrorControl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceManager {	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private EventDispatcher eventDispatcher = null;
	private HashMap<Class<? extends Service>, Service> services = null;
	
	public ServiceManager(Config config) {
		this.logger.info("Loading service manager");
		
		this.eventDispatcher = new EventDispatcher();
		
		setupServices(config);
	}
	
	@SuppressWarnings("unchecked")
	private void setupServices(Config config) {
		this.logger.info("Setting up services");
		
		this.services = new HashMap<Class<? extends Service>, Service>();
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(Service.class));

		while (itr.hasNext()) {
			Class<? extends Service> cls = (Class<? extends Service>) itr.next();
		
			if (cls.equals(Service.class)) continue;
		
			try {
				this.logger.info(String.format("Found service class %s", cls.getName()));
				
				Service service = cls.newInstance();
				
				this.services.put(cls, service);
			} catch (Exception e) {
				ErrorControl.logException(e);
			}
		}
		
		HashMap<Class<? extends Service>, Set<Class<? extends Service>>> dependencySet = new HashMap<Class<? extends Service>, Set<Class<? extends Service>>>();
		for (Service service: this.services.values()) {
			dependencySet.put(service.getClass(), service.getDependencies());
		}
		
		for (Entry<Class<? extends Service>, Set<Class<? extends Service>>> e: dependencySet.entrySet()) {
			for (Class<? extends Service> dependency: e.getValue()) {
				if (dependencySet.get(dependency).contains(e.getKey())) {
					ErrorControl.fatal(String.format("Found circular dependency between %s and %s", e.getKey().getName(), dependency.getName()));
				}
			}
		}
		
		HashSet<Class<? extends Service>> uninitializedServiceSet = new HashSet<Class<? extends Service>>(this.services.keySet());
		while (!uninitializedServiceSet.isEmpty()) {
			Iterator<Class<? extends Service>> serviceItr = uninitializedServiceSet.iterator();
			while (serviceItr.hasNext()) {
				Class<? extends Service> serviceClass = serviceItr.next();
				
				boolean ready = true;
				for (Class<? extends Service> dep: dependencySet.get(serviceClass)) {
					if (uninitializedServiceSet.contains(dep)) {
						ready = false;
						break;
					}
				}
				
				if (!ready) continue;
				
				this.logger.info(String.format("Initializing %s", serviceClass.getName()));
				
				this.services.get(serviceClass).load(this, config);
				
				serviceItr.remove();
			}
		}
		
		for (Service service: this.services.values()) {
			this.logger.info(String.format("Running post initialization on %s", service.getClass().getName()));
			service.postInit();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Service> T getService(Class<T> service) {
		return (T) this.services.get(service);
	}
	
	public EventDispatcher getEventDispatcher() { return this.eventDispatcher; }
	
}
