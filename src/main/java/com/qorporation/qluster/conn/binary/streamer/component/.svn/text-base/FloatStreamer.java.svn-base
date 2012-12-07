package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class FloatStreamer extends BinaryComponentStreamer<Float> {

	public FloatStreamer(EntityService service, Class<Float> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Float read(BinaryPayload data) {
		return Serialization.deserializeFloat(data.read(4));
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		return new BinaryPayload(name, ts, Serialization.serialize((Float) comp));
	}

}
