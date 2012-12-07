package com.qorporation.qluster.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.MetaKey;
import com.qorporation.qluster.entity.typesafety.FieldKey.FetchType;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.transaction.Transaction;
import com.qorporation.qluster.util.ErrorControl;

public abstract class Manager<T extends Definition<?>> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected EntityService entityService;
	protected Class<T> entityType = null;
	protected T def = null;

	protected List<FieldKey<?>> entityKeys = null;
	protected PrimaryKey<?, ?> primaryKey = null;
	protected List<MetaKey> metaKeys = null;
	
	protected EntityHelper<T> entityHelper = null;
	
	protected List<FieldKey<?>> prefetched = null;
	
	protected Entity<T> dummyEntity = null;
	
	@SuppressWarnings("unchecked")
	public void setup(EntityService entityService,
			Class<T> entityType, List<FieldKey<?>> keys,
			PrimaryKey<?, ?> primaryKey,
			List<MetaKey> metaKeys) {
		this.entityService = entityService;
		this.entityType = entityType;
		this.entityKeys = keys;
		this.primaryKey = primaryKey;
		this.metaKeys = metaKeys;
		
		this.entityHelper = new EntityHelper<T>();
		
		this.prefetched = new ArrayList<FieldKey<?>>();
		for (FieldKey<?> k: keys) {
			if (!k.prefetch().equals(FetchType.NONE)) {
				this.prefetched.add((FieldKey<Entity<?>>) k);
				this.logger.info(String.format("Added %s to prefetch list", k.getName()));
			}
		}
		
		try {
			this.def = entityType.newInstance();
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	}
	
	public void postFinalize() {
		this.dummyEntity = this.create();
	}
	
	public EntityService getEntityService() { return this.entityService; }
	public List<FieldKey<?>> getKeys() { return this.entityKeys; }
	public PrimaryKey<?, ?> getPrimaryKey() { return this.primaryKey; }
	public T getDefinition() { return this.def; }
	public List<FieldKey<?>> getPrefetched() { return this.prefetched; }
	public Entity<T> getDummy() { return this.dummyEntity; }
	
	public void materialize(List<Entity<T>> proxies) {
		List<String> keys = new ArrayList<String>(proxies.size());
		for (Entity<T> e: proxies) if (e != null) { keys.add(e.getKey()); }
		
		Map<String, Entity<T>> materialized = this.get(keys);
		for (int i = 0; i < proxies.size(); i++) {
			if (proxies.get(i) != null && materialized.get(proxies.get(i).getKey()) != null) {
				proxies.get(i).node = materialized.get(proxies.get(i).getKey()).node;
			}
		}
	}
	
	public abstract Entity<T> getProxy(String key);
	
	public abstract Entity<T> wrap(String key, Node<T> node);
	
	public Entity<T> create() { return create(null); }
	public abstract Entity<T> create(String key);
	
	public Entity<T> get(String key) { return this.get(Arrays.asList(key)).get(key); }
	public Map<String, Entity<T>> get(List<String> keys) { return get(keys, this.entityService.getGlobalTransaction()).getValue(); }
	public abstract Operation<Boolean, Map<String, Entity<T>>> get(List<String> keys, Transaction transaction);

	public boolean save(Entity<T> entity) { return save(entity, this.entityService.getGlobalTransaction()).getResult(); }
	public abstract Operation<Boolean, Void> save(Entity<T> entity, Transaction transaction);

	public void delete(String key) { delete(key, this.entityService.getGlobalTransaction()).execute(); }
	public abstract Operation<Boolean, Void> delete(String key, Transaction transaction);
	
	public EntityHelper<T> getHelper() { return this.entityHelper; }
	public List<String> getKeys(List<Entity<T>> entities) { return this.entityHelper.getKeys(entities); }
	public <K> List<K> getValues(FieldKey<K> key, List<Entity<T>> entities) { return this.entityHelper.getValues(key, entities); }
	
}