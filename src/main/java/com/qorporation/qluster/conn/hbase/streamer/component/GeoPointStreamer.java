package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.common.GeoPoint;
import com.qorporation.qluster.conn.hbase.streamer.HBaseColumnIndexStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class GeoPointStreamer extends HBaseComponentStreamer<GeoPoint> implements HBaseColumnIndexStreamer<GeoPoint> {

	public GeoPointStreamer(EntityService service, Class<GeoPoint> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public GeoPoint read(byte[] data) {
		byte[] bytes = new byte[8];
		
		System.arraycopy(data, 0, bytes, 0, 8);
		Double lat = Serialization.deserializeDouble(bytes);
		
		System.arraycopy(data, 0, bytes, 8, 8);
		Double lon = Serialization.deserializeDouble(bytes);
		
		return new GeoPoint(lat, lon);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		byte[] bytes = new byte[16];
		
		byte[] lat = Serialization.serialize(((GeoPoint) comp).getLat());
		byte[] lon = Serialization.serialize(((GeoPoint) comp).getLng());
		
		System.arraycopy(lat, 0, bytes, 0, 8);
		System.arraycopy(lon, 0, bytes, 8, 8);
		
		return bytes;
	}

}
