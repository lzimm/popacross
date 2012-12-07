package com.qorporation.qluster.entity.backend;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.ComponentStreamer;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Manager;

public abstract class EntityBackend<T extends Connection> {
    protected static final String ENTITY_PACKAGE = "com.qorporation.qluster.entity.definition";
    protected static final String MANAGER_PACKAGE = "com.qorporation.qluster.service.definition.entity.manager";
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
	public abstract Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> loadStreamers(Config config);
	public abstract Map<Class<? extends Definition<?>>, Manager<? extends Definition<?>>> loadManagers(Config config);
	public abstract void finalizeInitialization(Config config);
	
}
