package com.qorporation.qluster.conn.sql.generators;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.conn.sql.streamer.SQLMetaKeyStreamer;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.ErrorControl;

public abstract class SQLSchemaTableGenerator<T extends SQLBackedDefinition> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected EntityService entityService = null;
	protected Map<Class<?>, Class<? extends SQLComponentStreamer<?>>> streamers = null;
	protected Map<Class<?>, Class<? extends SQLMetaKeyStreamer<?>>> metaStreamers = null;
	
	protected void setup(EntityService entityService, Map<Class<?>, Class<? extends SQLComponentStreamer<?>>> streamers, Map<Class<?>, Class<? extends SQLMetaKeyStreamer<?>>> metaStreamers) {
		this.entityService = entityService;
		this.streamers = streamers;
		this.metaStreamers = metaStreamers;
	}
	
	protected SQLComponentStreamer<?> getStreamer(Class<?> type, ParameterizedType paramType) {
		try {
			Class<? extends SQLComponentStreamer<?>> cls = (Class<? extends SQLComponentStreamer<?>>) this.streamers.get(type);
			if (cls == null && Enum.class.isAssignableFrom(type)) {
				cls = (Class<? extends SQLComponentStreamer<?>>) this.streamers.get(Enum.class);
			}
			
			Constructor<? extends SQLComponentStreamer<?>> ctor = cls.getConstructor(EntityService.class, Class.class, ParameterizedType.class);
			SQLComponentStreamer<?> instance = ctor.newInstance(this.entityService, type, paramType);
			return instance;
		} catch (Exception e) {
			ErrorControl.fatal(String.format("Trying to use illegal type as column index: %s", type.getSimpleName()));
			return null;
		}
	}
	
	protected SQLMetaKeyStreamer<?> getMetaStreamer(Class<?> type) {
		try {
			Class<? extends SQLMetaKeyStreamer<?>> cls = (Class<? extends SQLMetaKeyStreamer<?>>) this.metaStreamers.get(type);
			Constructor<? extends SQLMetaKeyStreamer<?>> ctor = cls.getConstructor(EntityService.class);
			SQLMetaKeyStreamer<?> instance = ctor.newInstance(this.entityService);
			return instance;
		} catch (Exception e) {
			ErrorControl.fatal(String.format("Trying to use illegal type as column index: %s", type.getSimpleName()));
			return null;
		}
	}
	
	protected abstract void generate(Class<? extends SQLBackedDefinition> definition, PrintStream out);
	
}
