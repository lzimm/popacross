package com.qorporation.qluster.conn.binary.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.common.TimeUUID;
import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.conn.binary.streamer.BinaryComponentStreamer;
import com.qorporation.qluster.entity.EntityService;

public class TimeUUIDStreamer extends BinaryComponentStreamer<TimeUUID> {

	public TimeUUIDStreamer(EntityService service, Class<TimeUUID> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public TimeUUID read(BinaryPayload data) {
		return TimeUUID.fromBytes(data.read(16));
	}

	@Override
	public BinaryPayload write(Object comp, byte[] name, long ts) {
		return new BinaryPayload(name, ts, ((TimeUUID) comp).getBytes());
	}

}
