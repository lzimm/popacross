package com.qorporation.qluster.entity;

import java.util.List;
import java.util.Map;

import com.qorporation.qluster.common.DeltaTrackingList;
import com.qorporation.qluster.conn.Connection;
import com.qorporation.qluster.entity.typesafety.FieldKey;

@SuppressWarnings("unchecked")
public class Entity<T extends Definition<? extends Connection>> {

	protected EntityService service = null;
	protected Manager<T> manager = null;
	protected Class<T> type = null;
	protected String key = null;
	protected Node<T> node = null;
	
	public Entity(EntityService service, Class<T> type, String key, Node<T> node) {
		this.service = service;
		this.type = type;
		this.node = node;
		this.key = key;
		
		this.manager = this.service.getManager(this.type);
	}
	
	public Entity<T> branch() {
		return new Entity<T>(this.service, this.type, this.key, this.node.branch());
	}
	
	public Class<T> getType() { return this.type; }
	public Node<T> getNode() { return this.node; }
	
	public <K> boolean isDirty(FieldKey<K> key) {
		return this.getNode().isDirty(key.getName());
	}
	
	public <K> K getDirty(FieldKey<K> key) {
		return (K) this.getNode().getDirty(key.getName());
	}
	
	public <K> K get(FieldKey<K> key) { return get(key, null); }
	public <K> K get(FieldKey<K> key, Object defaultValue) {
		return (K) this.getNode().getProperty(key.getName(), defaultValue);
	}
	
	public <K> void set(FieldKey<K> key, K value) {
		this.getNode().setProperty(key.getName(), value);
	}
	
	public <K> void add(FieldKey<List<K>> key, K value) {
		List<K> list = (List<K>) this.getNode().getProperty(key.getName());
		
		if (list == null) {
			list = new DeltaTrackingList<K>();
			this.getNode().setProperty(key.getName(), list);
		}
		
		list.add((K) value);
		this.getNode().setProperty(key.getName(), list);
	}

	public void delete() {
		this.manager.delete(this.key);
	}
	
	public Map<String, Object> dict() {
		return this.node.getMap();
	}
	
	public Object val(String key) {
		return this.getNode().getProperty(key, null);
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null && this.getKey().equals(((Entity<?>) o).getKey());
	}
	
	@Override
	public int hashCode() {
		return this.getKey().hashCode();
	}

	public String getKey() {
		return this.key;
	}
	
	public void mark() {
		this.getNode().mark();
	}

	public void setKey(Object keyValue) {		
		this.key = keyValue.toString();
	}
	
}
