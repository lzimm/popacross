package com.qorporation.qluster.conn.sql.streamer.component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.ProxyEntity;
import com.qorporation.qluster.entity.annotation.AllowNull;

public class EntityStreamer<T extends Definition<?>> extends SQLComponentStreamer<Entity<T>> {

	private Class<T> entityType = null;
	
	@SuppressWarnings("unchecked")
	public EntityStreamer(EntityService service, Class<Entity<T>> type,
			ParameterizedType subType) {
		super(service, type, subType);
		
		this.entityType = (Class<T>) subType.getActualTypeArguments()[0];
	}

	@Override
	public Entity<T> read(Object data) {
		String id = data.toString();
		return new ProxyEntity<T>(service, entityType, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public String write(Object comp, byte[] name, long ts) {
		return ((Entity<T>) comp).getKey();
	}
	
	@Override
	public String generateColumnSchemaType(Field field) {
		return "INT(11)";
	}
	
	@Override
	public String generateColumnSchemaDefaultsAndConstraints(Field field) {
		if (field.getAnnotation(AllowNull.class) == null) {
			return "DEFAULT 0";
		} else {
			return "";
		}
	}

}
