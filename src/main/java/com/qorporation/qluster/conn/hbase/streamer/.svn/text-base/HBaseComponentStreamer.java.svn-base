package com.qorporation.qluster.conn.hbase.streamer;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.entity.ComponentStreamer;
import com.qorporation.qluster.entity.EntityService;

public abstract class HBaseComponentStreamer<C> extends ComponentStreamer<C, byte[], byte[]> {

	protected EntityService service = null;
	protected Class<C> type = null;
	protected ParameterizedType subType = null;
	
	public HBaseComponentStreamer(EntityService service, Class<C> type, ParameterizedType subType) {
		this.service = service;
		this.type = type;
		this.subType = subType;
	}
	
}
