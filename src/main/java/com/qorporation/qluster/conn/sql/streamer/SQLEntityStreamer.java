package com.qorporation.qluster.conn.sql.streamer;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qorporation.qluster.conn.sql.SQLConnection;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.FieldKey;
import com.qorporation.qluster.entity.typesafety.Key;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.EscapeUtil;
import com.qorporation.qluster.util.Serialization;

public class SQLEntityStreamer<T extends SQLBackedDefinition> {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	protected EntityService entityService = null;
	
	protected Class<T> entityType = null;
	protected List<FieldKey<?>> keys = null;
	
	protected Map<String, SQLComponentStreamer<?>> streamers = null;
	protected SQLPrimaryKeyStreamer<?> primaryStreamer = null;
	
	public SQLEntityStreamer(EntityService entityService, Class<T> entityType, List<FieldKey<?>> keys, PrimaryKey<?, ?> primaryKey) {
		this.entityService = entityService;
		this.entityType = entityType;
		
		this.keys = keys;
		
		this.streamers = new HashMap<String, SQLComponentStreamer<?>>();
		for (FieldKey<?> key: this.keys) {
			SQLComponentStreamer<?> streamer = generateStreamer(key);
			this.streamers.put(key.getName(), streamer);
		}
		
		SQLComponentStreamer<?> tmpPrimaryStreamer = generateStreamer(primaryKey);
		if (!SQLPrimaryKeyStreamer.class.isAssignableFrom(tmpPrimaryStreamer.getClass())) {
			ErrorControl.fatal(String.format("Using illegal primary key: %s for: %s", primaryKey.getType().getSimpleName(), entityType.getSimpleName()));
		} else {
			this.primaryStreamer = (SQLPrimaryKeyStreamer<?>) tmpPrimaryStreamer;
		}
	}

	@SuppressWarnings("unchecked")
	private SQLComponentStreamer<?> generateStreamer(Key<?> key) {
		SQLComponentStreamer<?> instance = null;
		
		try {
			Class<?> type = key.getType();
			ParameterizedType subType = key.getSubType();
			
			Class<? extends SQLComponentStreamer<?>> cls = this.entityService.getStreamer(SQLConnection.class, SQLComponentStreamer.class, type);
			if (cls == null && Enum.class.isAssignableFrom(type)) {
				cls = this.entityService.getStreamer(SQLConnection.class, SQLComponentStreamer.class, Enum.class);
			}
			
			Constructor<? extends SQLComponentStreamer<?>> ctor = cls.getConstructor(EntityService.class, Class.class, ParameterizedType.class);
			instance = ctor.newInstance(this.entityService, type, subType);
		} catch (Exception e) {
			ErrorControl.logException(e);
		}
	    
	    return instance;
	}

	public Map<String, String> stream(Entity<?> entity) {
		Map<String, String> streamed = new HashMap<String, String>();
		
		long ts = System.currentTimeMillis();
		for (FieldKey<?> key: this.keys) {
			String name = key.getName();

			if (entity.isDirty(key)) {
				if (entity.getDirty(key) != null) {
					streamed.put(name, EscapeUtil.escape(this.streamers.get(name).write(entity.get(key), Serialization.serialize(name), ts), EscapeUtil.BACKSLASH, EscapeUtil.MYSQL_CHARS));
				} else {
					streamed.put(name, null);
				}
			}
		}
		
		return streamed;
	}

	public Map<String, Object> parse(Map<String, Object> vals) {
		Map<String, Object> parsed = new HashMap<String, Object>();
		
		for (Entry<String, Object> e: vals.entrySet()) {
			if (e.getValue() == null) continue;
			
			String name = e.getKey();
			SQLComponentStreamer<?> streamer = this.streamers.get(name);
			if (streamer != null) {
				parsed.put(name, streamer.read(e.getValue()));
			}
		}
		
		return parsed;
	}
	
	public SQLPrimaryKeyStreamer<?> getPrimaryStreamer() {
		return this.primaryStreamer;
	}
	
	public SQLComponentStreamer<?> getStreamer(String field) {
		return this.streamers.get(field);
	}
	
}
