package com.qorporation.qluster.conn.hbase.streamer.component;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.hbase.streamer.HBaseColumnIndexStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBaseComponentStreamer;
import com.qorporation.qluster.conn.hbase.streamer.HBasePrimaryKeyStreamer;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class StringStreamer extends HBaseComponentStreamer<String> implements HBasePrimaryKeyStreamer<String>, HBaseColumnIndexStreamer<String> {

	public StringStreamer(EntityService service, Class<String> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public String read(byte[] data) {
		return Serialization.deserializeString(data);
	}

	@Override
	public byte[] write(Object comp, byte[] name, long ts) {
		return Serialization.serialize((String) comp);
	}
	
	@Override
	public String generateKey() {
		ErrorControl.fatal(String.format("Attempted to use %s to generate key", this.type.getName()));
		return null;		
	}

}
