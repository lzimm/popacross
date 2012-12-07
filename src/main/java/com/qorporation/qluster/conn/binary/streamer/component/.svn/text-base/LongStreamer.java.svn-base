package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class LongStreamer extends BinaryComponentStreamer<Long> {

	public LongStreamer(EntityService service, Class<Long> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Long read(BinaryPayload data) {
		return Serialization.deserializeLong(data.read(8));
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		return new BinaryPayload(name, ts, Serialization.serialize((Long) comp));
	}

}
