package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class DoubleStreamer extends HBaseComponentStreamer<Double> {

	public DoubleStreamer(EntityService service, Class<Double> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Double read(byte[] data) {
		return Serialization.deserializeDouble(data);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return Serialization.serialize((Double) comp);
	}

}
