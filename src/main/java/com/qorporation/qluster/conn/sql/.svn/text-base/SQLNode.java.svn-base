package com.qorporation.qluster.conn.sql;

import java.util.HashMap;
import java.util.Map;

import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Manager;
import com.qorporation.qluster.entity.Node;

public class SQLNode<T extends Definition<?>> extends Node<T> {

	volatile private boolean created = false;
	
	public SQLNode(T def, Manager<T> manager, Map<String, Object> map, boolean created) {
		super(def, manager, map);
		this.created = created;
	}
	
	@Override
	public Node<T> branch() {
		return new SQLNode<T>(this.def, this.manager, new HashMap<String, Object>(this.map), this.created);
	}
	
	public void markCreated(boolean created) {
		this.created = created;
	}
	
	public boolean isCreated() {
		return this.created;
	}

}
