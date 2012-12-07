package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.hbase.streamer.HBaseColumnIndexStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBasePrimaryKeyStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class LongStreamer extends HBaseComponentStreamer<Long> implements HBasePrimaryKeyStreamer<Long>, HBaseColumnIndexStreamer<Long> {

	public LongStreamer(EntityService service, Class<Long> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public Long read(byte[] data) {
		return Serialization.deserializeLong(data);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return Serialization.serialize((Long) comp);
	}
	
	@Override
	public String generateKey() {
		ErrorControl.fatal(String.format("Attempted to use %s to generate key", this.type.getName()));
		return null;		
	}

}
