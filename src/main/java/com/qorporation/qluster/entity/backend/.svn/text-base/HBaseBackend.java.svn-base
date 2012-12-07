package com.qorporation.qluster.entity.backend;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.config.Config;
import com.qorporation.qluster.conn.hbase.HBaseBackedEntityManager;
import com.qorporation.qluster.conn.hbase.HBaseConnection;
import com.qorporation.qluster.conn.hbase.generators.HBaseSchemaGenerator;
import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.entity.ComponentStreamer;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.MetaKey;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.util.ClassWalker;
import com.qorporation.qluster.util.ClassWalkerFilter;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Reflection;
import com.qorporation.qluster.util.RelativePath;

public class HBaseBackend extends EntityBackend<HBaseConnection> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private EntityService entityService;
    
	private Map<Class<? extends HBaseBackedDefinition>, Manager<? extends HBaseBackedDefinition>> managers = null;
	private Map<Class<?>, Class<? extends HBaseComponentStreamer<?>>> componentStreamers = null;
	
	public HBaseBackend(EntityService entityService) {
		this.logger.info("Loading hbase entity backend");
		
		this.entityService = entityService;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Class<? extends HBaseBackedDefinition>, Class<? extends HBaseBackedEntityManager<?>>> findManagers() {
		this.logger.info("Finding entity managers");
		Map<Class<? extends HBaseBackedDefinition>, Class<? extends HBaseBackedEntityManager<?>>> managers = new HashMap<Class<? extends HBaseBackedDefinition>, Class<? extends HBaseBackedEntityManager<?>>>();
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(HBaseBackedEntityManager.class));
		
		while (itr.hasNext()) {
			Class<? extends HBaseBackedEntityManager<?>> cls = (Class<? extends HBaseBackedEntityManager<?>>) itr.next();
			
			this.logger.info(String.format("Found entity manager: %s", cls.getName()));
			
		    Class<?> paramType = Reflection.getParamType(cls, 0);
	    	Class<? extends HBaseBackedDefinition> definition = (Class<? extends HBaseBackedDefinition>) paramType;
	    	managers.put(definition, cls);
		}
		
		return managers;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupEntities() {
		this.managers = new HashMap<Class<? extends HBaseBackedDefinition>, Manager<? extends HBaseBackedDefinition>>();
		
		Map<Class<? extends HBaseBackedDefinition>, Class<? extends HBaseBackedEntityManager<?>>> managers = findManagers();
		Map<Class<? extends HBaseBackedDefinition>, List<FieldKey<?>>> keysByClass = new HashMap<Class<? extends HBaseBackedDefinition>, List<FieldKey<?>>>();
		Map<Class<? extends HBaseBackedDefinition>, PrimaryKey<?, ?>> primaryKeys = new HashMap<Class<? extends HBaseBackedDefinition>, PrimaryKey<?, ?>>();
		Map<Class<? extends HBaseBackedDefinition>, List<MetaKey>> metaKeysByClass = new HashMap<Class<? extends HBaseBackedDefinition>, List<MetaKey>>();
		
		this.logger.info("Setting up entities");
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.isConcreteClass(),
												ClassWalkerFilter.extending(HBaseBackedDefinition.class));
		
		while (itr.hasNext()) {
			Class<? extends HBaseBackedDefinition> cls = (Class<? extends HBaseBackedDefinition>) itr.next();
			
			this.logger.info(String.format("Setting up entity: %s", cls.getName()));
			
			List<FieldKey<?>> keys = new ArrayList<FieldKey<?>>();
			PrimaryKey<?, ?> primaryKey = null;
			List<MetaKey> metaKeys = new ArrayList<MetaKey>();
			
			for (Field field: cls.getDeclaredFields()) {
				try {
					if (field.getType().equals(FieldKey.class)) {
					    Class<?> paramType = Reflection.getParamType(field, 0);
					    ParameterizedType paramSubType = Reflection.getParamSubType(field, 0);
					    FieldKey<?> key = new FieldKey(cls, paramType, paramSubType, field.getName(), field.getAnnotations());
						field.set(null, key);
						keys.add(key);
					} else if (field.getType().equals(PrimaryKey.class)) {
					    Class<?> paramType = Reflection.getParamType(field, 0);
					    ParameterizedType paramSubType = Reflection.getParamSubType(field, 0);
					    primaryKey = new PrimaryKey(cls, paramType, paramSubType, field.getName());
						field.set(null, primaryKey);						
					} else if (MetaKey.class.isAssignableFrom(field.getType())) {
						MetaKey key = new MetaKey(cls, field.getName(), field.getAnnotations());
						metaKeys.add(key);
					}
				} catch (Exception e) {
					ErrorControl.logException(e);
				}
			}
			
			keysByClass.put(cls, keys);
			primaryKeys.put(cls, primaryKey);
			metaKeysByClass.put(cls, metaKeys);
			
			HBaseBackedEntityManager em = null;
			Class<? extends HBaseBackedEntityManager<?>> manager = managers.get(cls);
			if (manager != null) {
				try {
					em = manager.newInstance();
					this.logger.info(String.format("Using manager: %s for: %s", manager.getName(), cls.getName()));
				} catch (Exception e) {
					ErrorControl.logException(e);
					em = null;
				}
			}
			
			if (em == null) {
				em = new HBaseBackedEntityManager();
				this.logger.info(String.format("Using generic manager for: %s", cls.getName()));
			}
			
			this.managers.put(cls, em);
		}
		
		for (Entry<Class<? extends HBaseBackedDefinition>, Manager<? extends HBaseBackedDefinition>> entry: this.managers.entrySet()) {
			Class<? extends Definition> cls = entry.getKey();
			Manager em = entry.getValue();
			List<FieldKey<?>> keys = keysByClass.get(cls);
			PrimaryKey<?, ?> primaryKey = primaryKeys.get(cls);
			List<MetaKey> metaKeys = metaKeysByClass.get(cls);
			em.setup(this.entityService, cls, keys, primaryKey, metaKeys);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setupStreamers() {
		this.componentStreamers = new HashMap<Class<?>, Class<? extends HBaseComponentStreamer<?>>>();
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(HBaseComponentStreamer.class));
		
		while (itr.hasNext()) {
			Class<? extends HBaseComponentStreamer<?>> cls = (Class<? extends HBaseComponentStreamer<?>>) itr.next();
		    Class<?> paramType = Reflection.getParamType(cls, 0);
		    
		    this.logger.info(String.format("Found component streamer: %s", cls.getName()));
		    this.componentStreamers.put(paramType, cls);
		}
	}
	
	private void generateSchema() {
		File schemaFile = new File(RelativePath.root().getAbsolutePath()
								.concat(File.separator)
								.concat("schema")
								.concat(File.separator)
								.concat("hbase-conf.xml"));
		
		HBaseSchemaGenerator generator = new HBaseSchemaGenerator(this.entityService, 
													this.managers.keySet(), 
													this.componentStreamers);

		generator.generate(schemaFile);
	}

	@Override
	public Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> loadStreamers(Config config) {
		setupStreamers();
		
		Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> ret = new HashMap<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>>();
		for (Entry<Class<?>, Class<? extends HBaseComponentStreamer<?>>> e: this.componentStreamers.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}
	
	@Override
	public Map<Class<? extends Definition<?>>, Manager<? extends Definition<?>>> loadManagers(Config config) {
		setupEntities();
		
		Map<Class<? extends Definition<?>>, Manager<? extends Definition<?>>> ret = new HashMap<Class<? extends Definition<?>>, Manager<? extends Definition<?>>>();
		for (Entry<Class<? extends HBaseBackedDefinition>, Manager<? extends HBaseBackedDefinition>> e: this.managers.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}
	
	@Override
	public void finalizeInitialization(Config config) {
		generateSchema();
	}

}
