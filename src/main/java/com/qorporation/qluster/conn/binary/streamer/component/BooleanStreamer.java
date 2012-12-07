package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class BooleanStreamer extends BinaryComponentStreamer<Boolean> {
	private static int BOOLEAN_SIZE = Serialization.serialize(Boolean.TRUE).length;
	
	public BooleanStreamer(EntityService service, Class<Boolean> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Boolean read(BinaryPayload data) {
		return Serialization.deserializeBoolean(data.read(BOOLEAN_SIZE));
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		return new BinaryPayload(name, ts, Serialization.serialize((Boolean) comp));
	}
	
}
