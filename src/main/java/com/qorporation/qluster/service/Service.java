package com.qorporation.qluster.service;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.config.Config;

public abstract class Service {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected EventDispatcher eventDispatcher;
	
	@SuppressWarnings("unchecked")
	public Set<Class<? extends Service>> getDependencies() {
		HashSet<Class<? extends Service>> dependencies = new HashSet<Class<? extends Service>>();
		
		for (Field f: getClass().getDeclaredFields()) {
			if (Service.class.isAssignableFrom(f.getType())) {
				this.logger.info(String.format("Found dependency: %s", f.getType().getName()));
				dependencies.add((Class<? extends Service>) f.getType());
			}
		}
		
		return dependencies;
	}
	
	public void load(ServiceManager serviceManager, Config config) {
		this.eventDispatcher = serviceManager.getEventDispatcher();
		this.init(serviceManager, config);
	}
	
	public void init(ServiceManager serviceManager, Config config) {}
	public void postInit() {}
	
}
