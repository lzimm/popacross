package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBasePrimaryKeyStreamer;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.ProxyEntity;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class EntityStreamer<T extends Definition<?>> extends HBaseComponentStreamer<Entity<T>> implements HBasePrimaryKeyStreamer<Entity<T>> {

	private Class<T> entityType = null;
	
	@SuppressWarnings("unchecked")
	public EntityStreamer(EntityService service, Class<Entity<T>> type,
			ParameterizedType subType) {
		super(service, type, subType);
		
		this.entityType = (Class<T>) subType.getActualTypeArguments()[0];
	}

	@Override
	public Entity<T> read(byte[] data) {
		String id = Serialization.deserializeString(data);
		return new ProxyEntity<T>(service, entityType, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return Serialization.serialize(((Entity<T>) comp).getKey());
	}

	@Override
	public String generateKey() {
		ErrorControl.fatal(String.format("Attempted to use %s to generate key", this.type.getName()));
		return null;		
	}

}
