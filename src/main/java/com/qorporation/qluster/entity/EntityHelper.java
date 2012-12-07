package com.qorporation.qluster.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.qorporation.qluster.common.ConstructingHashMap;
import com.qorporation.qluster.common.constructor.ArrayListConstructor;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class EntityHelper<T extends Definition<?>> {
	private static final List<FieldKey<?>> EMPTY_KEY_LIST = new LinkedList<FieldKey<?>>();
	private static final List<RecursiveFieldKeySet<?>> EMPTY_RECURSIVE_LIST = new LinkedList<RecursiveFieldKeySet<?>>();
	
	public static final class RecursiveFieldKeySet<K> {
		public FieldKey<K> key = null;
		public List<FieldKey<?>> filtered = EntityHelper.EMPTY_KEY_LIST;
		public List<RecursiveFieldKeySet<?>> recursive = EntityHelper.EMPTY_RECURSIVE_LIST;
		
		public RecursiveFieldKeySet(FieldKey<K> key) { this.key = key; }
		public RecursiveFieldKeySet(FieldKey<K> key, FieldKey<?> ... filtered) { this.key = key; this.filtered = Arrays.asList(filtered); }
		public RecursiveFieldKeySet(FieldKey<K> key, RecursiveFieldKeySet<?> ... recursive) { this.key = key; this.recursive = Arrays.asList(recursive); }
		public RecursiveFieldKeySet(FieldKey<K> key, List<FieldKey<?>> filtered, List<RecursiveFieldKeySet<?>> recursive) { this.key = key; this.filtered = filtered; this.recursive = recursive; }
		
		public void swap(Map<String, Object> map) {
			Entity<?> entity = (Entity<?>) map.remove(this.key.getName());
			if (entity != null) {
				Map<String, Object> m = entity.getNode().getMap();
				
				for (FieldKey<?> f: this.filtered) {
					m.remove(f.getName());
				}
				
				for (RecursiveFieldKeySet<?> r: this.recursive) {
					r.swap(m);
				}
				
				map.put(this.key.getName(), m);
			}
		}
	}
	
	public <K> List<K> getValues(FieldKey<K> key, List<Entity<T>> entities) {
		List<K> ret = new ArrayList<K>(entities.size());
		
		for (Entity<T> e: entities) {
			ret.add(e.get(key));
		}
		
		return ret;
	}
	
	public <E extends Definition<?>> Map<String, Entity<E>> getValueMap(FieldKey<Entity<E>> key, List<Entity<T>> entities) {
		Map<String, Entity<E>> ret = new HashMap<String, Entity<E>>(entities.size());
		
		Entity<E> v = null;
		for (Entity<T> e: entities) {
			v = e.get(key);
			ret.put(v.getKey(), v);
		}
		
		if (v != null) {
			v.manager.materialize(new ArrayList<Entity<E>>(ret.values()));
		}
		
		return ret;
	}
	
	public List<String> getKeys(List<Entity<T>> entities) {
		List<String> ret = new ArrayList<String>(entities.size());
		
		for (Entity<T> e: entities) {
			ret.add(e.getKey());
		}
		
		return ret;
	}

	public <K> List<Entity<T>> filter(Collection<Entity<T>> c, FieldKey<K> key, K value) {
		List<Entity<T>> ret = new ArrayList<Entity<T>>(c.size());
		
		for (Entity<T> e: c) {
			if (e.get(key).equals(value)) {
				ret.add(e);
			}
		}
		
		return ret;
	}

	public <K> Map<K, List<Entity<T>>> map(FieldKey<K> key, List<Entity<T>> entities) {
		Map<K, List<Entity<T>>> ret = new ConstructingHashMap<K, List<Entity<T>>>(new ArrayListConstructor<K, Entity<T>>());
		
		for (Entity<T> e: entities) {
			ret.get(e.get(key)).add(e);
		}
		
		return ret;
	}

	public List<Map<String, Object>> getListOfMaps(List<Entity<T>> entities) { return this.getListOfMaps(entities, EntityHelper.EMPTY_KEY_LIST, EntityHelper.EMPTY_RECURSIVE_LIST); }
	public List<Map<String, Object>> getListOfMaps(List<Entity<T>> entities, List<FieldKey<?>> filtered) { return this.getListOfMaps(entities, filtered, EntityHelper.EMPTY_RECURSIVE_LIST); }
	public List<Map<String, Object>> getListOfMaps(List<Entity<T>> entities, List<FieldKey<?>> filtered, List<RecursiveFieldKeySet<?>> recursive) {
		List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>(entities.size());
		for (Entity<T> entity: entities) {
			Map<String, Object> map = entity.getNode().getMap();
			
			for (FieldKey<?> key: filtered) {
				map.remove(key.getName());
			}
			
			for (RecursiveFieldKeySet<?> set: recursive) {
				set.swap(map);
			}
			
			ret.add(map);
		}
		
		return ret;
	}

}
