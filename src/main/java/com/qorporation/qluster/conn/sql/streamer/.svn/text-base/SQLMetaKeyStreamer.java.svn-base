package com.qorporation.qluster.conn.sql.streamer;

import java.lang.reflect.Field;

import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.MetaKey;

public abstract class SQLMetaKeyStreamer<M extends MetaKey> {

	protected EntityService service = null;
	protected MetaKey metaKey = null;

	public SQLMetaKeyStreamer(EntityService service) {
		this.service = service;
	}
	
	public abstract String generateSchema(Class<? extends SQLBackedDefinition> definition, Field field);
	
}
