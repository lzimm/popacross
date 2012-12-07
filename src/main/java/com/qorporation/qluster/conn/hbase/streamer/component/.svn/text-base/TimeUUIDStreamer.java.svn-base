package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.common.TimeUUID;
import com.qorporation.qluster.conn.hbase.streamer.HBaseColumnIndexStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBasePrimaryKeyStreamer;
import com.qorporation.qluster.entity.EntityService;

public class TimeUUIDStreamer extends HBaseComponentStreamer<TimeUUID> implements HBasePrimaryKeyStreamer<TimeUUID>, HBaseColumnIndexStreamer<TimeUUID> {

	public TimeUUIDStreamer(EntityService service, Class<TimeUUID> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public TimeUUID read(byte[] data) {
		return TimeUUID.fromBytes(data);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return ((TimeUUID) comp).getBytes();
	}

	@Override
	public String generateKey() {
		return (new TimeUUID()).toString();
	}

}
