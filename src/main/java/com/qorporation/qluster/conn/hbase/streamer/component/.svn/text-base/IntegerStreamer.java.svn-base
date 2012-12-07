package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.hbase.streamer.HBaseColumnIndexStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.Serialization;

public class IntegerStreamer extends HBaseComponentStreamer<Integer> implements HBaseColumnIndexStreamer<Integer> {

	public IntegerStreamer(EntityService service, Class<Integer> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Integer read(byte[] data) {
		return Serialization.deserializeInt(data);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return Serialization.serialize((Integer) comp);
	}

}
