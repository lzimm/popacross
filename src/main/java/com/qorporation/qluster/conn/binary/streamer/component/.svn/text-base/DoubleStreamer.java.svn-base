package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class DoubleStreamer extends BinaryComponentStreamer<Double> {

	public DoubleStreamer(EntityService service, Class<Double> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Double read(BinaryPayload data) {
		return Serialization.deserializeDouble(data.read(8));
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		return new BinaryPayload(name, ts, Serialization.serialize((Double) comp));
	}

}
