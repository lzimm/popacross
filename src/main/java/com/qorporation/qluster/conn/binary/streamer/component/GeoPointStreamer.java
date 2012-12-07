package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class GeoPointStreamer extends BinaryComponentStreamer<GeoPoint> {

	public GeoPointStreamer(EntityService service, Class<GeoPoint> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public GeoPoint read(BinaryPayload data) {
		Double lat = Serialization.deserializeDouble(data.read(8));
		Double lon = Serialization.deserializeDouble(data.read(8));
		
		return new GeoPoint(lat, lon);
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		byte[] bytes = new byte[16];
		
		byte[] lat = Serialization.serialize(((GeoPoint) comp).getLat());
		byte[] lon = Serialization.serialize(((GeoPoint) comp).getLng());
		
		System.arraycopy(lat, 0, bytes, 0, 8);
		System.arraycopy(lon, 0, bytes, 8, 8);
		
		return new BinaryPayload(name, ts, bytes);
	}

}
