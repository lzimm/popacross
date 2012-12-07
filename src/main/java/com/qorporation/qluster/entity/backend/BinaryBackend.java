package com.qorporation.qluster.entity.backend;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.binary.BinaryConnection;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.ComponentStreamer;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.ClassWalkerFilter;
import com.qorporation.qluster.util.Reflection;

public class BinaryBackend extends EntityBackend<BinaryConnection> {    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
	private Map<Class<?>, Class<? extends BinaryComponentStreamer<?>>> componentStreamers = null;
	
	public BinaryBackend(EntityService entityService) {
		this.logger.info("Loading binary entity backend");
	}
	
	@SuppressWarnings("unchecked")
	private void setupStreamers() {
		this.componentStreamers = new HashMap<Class<?>, Class<? extends BinaryComponentStreamer<?>>>();
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(BinaryComponentStreamer.class));
		
		while (itr.hasNext()) {
			Class<? extends BinaryComponentStreamer<?>> cls = (Class<? extends BinaryComponentStreamer<?>>) itr.next();
		    Class<?> paramType = Reflection.getParamType(cls, 0);
		    
		    this.logger.info(String.format("Found component streamer: %s", cls.getName()));
		    this.componentStreamers.put(paramType, cls);
		}
	}

	@Override
	public Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> loadStreamers(Config config) {
		setupStreamers();
		
		Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> ret = new HashMap<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>>();
		for (Entry<Class<?>, Class<? extends BinaryComponentStreamer<?>>> e: this.componentStreamers.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}
	
	@Override
	public Map<Class<? extends Definition<?>>, Manager<? extends Definition<?>>> loadManagers(Config config) {
		return new HashMap<Class<? extends Definition<?>>, Manager<? extends Definition<?>>>();
	}
	
	@Override
	public void finalizeInitialization(Config config) {
	}

}
