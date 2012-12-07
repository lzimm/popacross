package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class FloatStreamer extends HBaseComponentStreamer<Float> {

	public FloatStreamer(EntityService service, Class<Float> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Float read(byte[] data) {
		return Serialization.deserializeFloat(data);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return Serialization.serialize((Float) comp);
	}

}
