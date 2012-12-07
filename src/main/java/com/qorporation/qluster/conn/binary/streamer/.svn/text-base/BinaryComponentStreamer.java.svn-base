package com.qorporation.qluster.conn.binary.streamer;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.binary.BinaryPayload;
import com.qorporation.qluster.entity.ComponentStreamer;
import com.qorporation.qluster.entity.EntityService;

public abstract class BinaryComponentStreamer<C> extends ComponentStreamer<C, BinaryPayload, BinaryPayload> {

	protected EntityService service = null;
	protected Class<C> type = null;
	protected ParameterizedType subType = null;
	
	public BinaryComponentStreamer(EntityService service, Class<C> type, ParameterizedType subType) {
		this.service = service;
		this.type = type;
		this.subType = subType;
	}

}
