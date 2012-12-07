package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class IntegerStreamer extends BinaryComponentStreamer<Integer> {

	public IntegerStreamer(EntityService service, Class<Integer> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Integer read(BinaryPayload data) {
		return Serialization.deserializeInt(data.read(4));
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		return new BinaryPayload(name, ts, Serialization.serialize((Integer) comp));
	}

}
