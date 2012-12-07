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
import com.qorporation.qluster.conn.sql.SQLBackedEntityManager;
import com.qorporation.qluster.conn.sql.SQLConnection;
import com.qorporation.qluster.conn.sql.generators.SQLSchemaGenerator;
import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.conn.sql.streamer.SQLMetaKeyStreamer;
import com.qorporation.qluster.conn.sql.streamer.SQLPrimaryKeyStreamer;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
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

public class SQLBackend extends EntityBackend<SQLConnection> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private EntityService entityService;
    
	private Map<Class<? extends SQLBackedDefinition>, Manager<? extends SQLBackedDefinition>> managers = null;
	private Map<Class<?>, Class<? extends SQLComponentStreamer<?>>> componentStreamers = null;
	private Map<Class<?>, Class<? extends SQLMetaKeyStreamer<?>>> metaKeyStreamers = null;
	
	public SQLBackend(EntityService entityService) {
		this.logger.info("Loading sql entity backend");
		
		this.entityService = entityService;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Class<? extends SQLBackedDefinition>, Class<? extends SQLBackedEntityManager<?>>> findManagers() {
		this.logger.info("Finding entity managers");
		Map<Class<? extends SQLBackedDefinition>, Class<? extends SQLBackedEntityManager<?>>> managers = new HashMap<Class<? extends SQLBackedDefinition>, Class<? extends SQLBackedEntityManager<?>>>();
		
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.extending(SQLBackedEntityManager.class));
		
		while (itr.hasNext()) {
			Class<? extends SQLBackedEntityManager<?>> cls = (Class<? extends SQLBackedEntityManager<?>>) itr.next();
			
			this.logger.info(String.format("Found entity manager: %s", cls.getName()));
			
		    Class<?> paramType = Reflection.getParamType(cls, 0);
	    	Class<? extends SQLBackedDefinition> definition = (Class<? extends SQLBackedDefinition>) paramType;
	    	managers.put(definition, cls);
		}
		
		return managers;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void setupEntities() {
		this.managers = new HashMap<Class<? extends SQLBackedDefinition>, Manager<? extends SQLBackedDefinition>>();
		
		Map<Class<? extends SQLBackedDefinition>, Class<? extends SQLBackedEntityManager<?>>> managers = findManagers();
		Map<Class<? extends SQLBackedDefinition>, List<FieldKey<?>>> keysByClass = new HashMap<Class<? extends SQLBackedDefinition>, List<FieldKey<?>>>();
		Map<Class<? extends SQLBackedDefinition>, PrimaryKey<?, ?>> primaryKeys = new HashMap<Class<? extends SQLBackedDefinition>, PrimaryKey<?, ?>>();
		Map<Class<? extends SQLBackedDefinition>, List<MetaKey>> metaKeysByClass = new HashMap<Class<? extends SQLBackedDefinition>, List<MetaKey>>();
		
		this.logger.info("Setting up entities");
		Iterator<Class<?>> itr = new ClassWalker(ClassWalkerFilter.isConcreteClass(),
												ClassWalkerFilter.extending(SQLBackedDefinition.class));
		
		while (itr.hasNext()) {
			Class<? extends SQLBackedDefinition> cls = (Class<? extends SQLBackedDefinition>) itr.next();
			
			this.logger.info(String.format("Setting up entity: %s", cls.getName()));
			
			List<FieldKey<?>> keys = new ArrayList<FieldKey<?>>();
			PrimaryKey<?, ?> primaryKey = null;
			List<MetaKey> metaKeys = new ArrayList<MetaKey>();
			
			for (Field field: cls.getFields()) {
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
			
			SQLBackedEntityManager em = null;
			Class<? extends SQLBackedEntityManager<?>> manager = managers.get(cls);
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
				em = new SQLBackedEntityManager();
				this.logger.info(String.format("Using generic manager for: %s", cls.getName()));
			}
			
			this.managers.put(cls, em);
		}
		
		for (Entry<Class<? extends SQLBackedDefinition>, Manager<? extends SQLBackedDefinition>> entry: this.managers.entrySet()) {
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
		this.componentStreamers = new HashMap<Class<?>, Class<? extends SQLComponentStreamer<?>>>();
		
		Iterator<Class<?>> compItr = new ClassWalker(ClassWalkerFilter.extending(SQLComponentStreamer.class),
												ClassWalkerFilter.excluding(SQLPrimaryKeyStreamer.class));
		
		while (compItr.hasNext()) {
			Class<? extends SQLComponentStreamer<?>> cls = (Class<? extends SQLComponentStreamer<?>>) compItr.next();
		    Class<?> paramType = Reflection.getParamType(cls, 0);
		    
		    this.logger.info(String.format("Found component streamer: %s", cls.getName()));
		    this.componentStreamers.put(paramType, cls);
		}
		
		this.metaKeyStreamers = new HashMap<Class<?>, Class<? extends SQLMetaKeyStreamer<?>>>();
		
		Iterator<Class<?>> metaItr = new ClassWalker(ClassWalkerFilter.extending(SQLMetaKeyStreamer.class));
		
		while (metaItr.hasNext()) {
			Class<? extends SQLMetaKeyStreamer<?>> cls = (Class<? extends SQLMetaKeyStreamer<?>>) metaItr.next();
		    Class<?> paramType = Reflection.getParamType(cls, 0);
		    
		    this.logger.info(String.format("Found meta streamer: %s", cls.getName()));
		    this.metaKeyStreamers.put(paramType, cls);
		}
	}
	
	private void generateSchema() {
		File schemaFile = new File(RelativePath.root().getAbsolutePath()
								.concat(File.separator)
								.concat("schema")
								.concat(File.separator)
								.concat("table-conf.sql"));
		
		SQLSchemaGenerator generator = new SQLSchemaGenerator(this.entityService, 
													this.managers.keySet(), 
													this.componentStreamers,
													this.metaKeyStreamers);

		generator.generate(schemaFile);
	}

	@Override
	public Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> loadStreamers(Config config) {
		setupStreamers();
		
		Map<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>> ret = new HashMap<Class<?>, Class<? extends ComponentStreamer<?, ?, ?>>>();
		for (Entry<Class<?>, Class<? extends SQLComponentStreamer<?>>> e: this.componentStreamers.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}
	
	@Override
	public Map<Class<? extends Definition<?>>, Manager<? extends Definition<?>>> loadManagers(Config config) {
		setupEntities();
		
		Map<Class<? extends Definition<?>>, Manager<? extends Definition<?>>> ret = new HashMap<Class<? extends Definition<?>>, Manager<? extends Definition<?>>>();
		for (Entry<Class<? extends SQLBackedDefinition>, Manager<? extends SQLBackedDefinition>> e: this.managers.entrySet()) {
			ret.put(e.getKey(), e.getValue());
		}
		return ret;
	}
	
	@Override
	public void finalizeInitialization(Config config) {
		generateSchema();
	}

}
