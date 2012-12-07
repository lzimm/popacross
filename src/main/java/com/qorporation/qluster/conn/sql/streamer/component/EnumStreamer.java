package com.qorporation.qluster.conn.sql.streamer.component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import sun.misc.SharedSecrets;

import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.entity.EntityService;

public class EnumStreamer<E extends Enum<E>> extends SQLComponentStreamer<E> {

	private E[] choiceSet = null;
	
	public EnumStreamer(EntityService service, Class<E> type,
			ParameterizedType subType) {
		super(service, type, subType);
		this.choiceSet = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(type);
	}

	@Override
	public E read(Object data) {
		return this.choiceSet[(Integer) data];
	}

	@SuppressWarnings("unchecked")
	@Override
	public String write(Object comp, byte[] name, long ts) {
		return Integer.toString(((E) comp).ordinal());
	}
	
	@Override
	public String generateColumnSchemaType(Field field) {
		return "INT(11)";
	}
	
	@Override
	public String generateColumnSchemaDefaultsAndConstraints(Field field) {
		return "DEFAULT 0";
	}
	
}
