package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.BinaryConnection;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.Entity;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.ProxyEntity;
import com.qorporation.qluster.util.Serialization;

public class EntityStreamer<T extends Definition<BinaryConnection>> extends BinaryComponentStreamer<Entity<T>> {

	private Class<T> entityType = null;
	
	@SuppressWarnings("unchecked")
	public EntityStreamer(EntityService service, Class<Entity<T>> type,
			ParameterizedType subType) {
		super(service, type, subType);
		
		this.entityType = (Class<T>) subType.getActualTypeArguments()[0];
	}

	@Override
	public Entity<T> read(BinaryPayload data) {
		int len = Serialization.deserializeInt(data.read(4));
		String id = Serialization.deserializeString(data.read(len));
		return new ProxyEntity<T>(service, entityType, id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		byte[] bytes = Serialization.serialize(((Entity<T>) comp).getKey());
		byte[] len = Serialization.serialize(bytes.length);
		return new BinaryPayload(name, ts, len, bytes);
	}

}
