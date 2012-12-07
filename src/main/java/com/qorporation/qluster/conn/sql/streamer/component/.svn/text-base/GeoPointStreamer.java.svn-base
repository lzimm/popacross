package com.qorporation.qluster.conn.sql.streamer.component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.entity.EntityService;

public class GeoPointStreamer extends SQLComponentStreamer<GeoPoint> {
	
	public GeoPointStreamer(EntityService service, Class<GeoPoint> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public GeoPoint read(Object data) {
		return new GeoPoint(data.toString());
	}

	@Override
	public String write(Object comp, byte[] name, long ts) {
		return ((GeoPoint) comp).geohash();
	}
	
	@Override
	public String generateColumnSchemaType(Field field) {
		return "VARCHAR(64)";
	}
	
	@Override
	public String generateColumnSchemaDefaultsAndConstraints(Field field) {
		return "DEFAULT ''";
	}

}
