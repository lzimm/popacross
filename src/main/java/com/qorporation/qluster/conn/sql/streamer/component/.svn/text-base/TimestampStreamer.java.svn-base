package com.qorporation.qluster.conn.sql.streamer.component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Timestamp;

import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Column;
import com.qorporation.qluster.entity.EntityService;

public class TimestampStreamer extends SQLComponentStreamer<Timestamp> {
	public static final String TIMESTAMP_BOTTOM_RANGE = "1970-01-01 00:00:01";
	
	public TimestampStreamer(EntityService service, Class<Timestamp> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public String write(Object comp, byte[] name, long ts) {
		return comp.toString();
	}
	
	@Override
	public String generateColumnSchemaType(Field field) {		
		return "TIMESTAMP";
	}

	@Override
	public String generateColumnSchemaDefaultsAndConstraints(Field field) {
		Column c = field.getAnnotation(Column.class);
		if (c != null) {
			if (c.autoUpdateTime()) {
				return String.format("DEFAULT '%s' ON UPDATE CURRENT_TIMESTAMP", TIMESTAMP_BOTTOM_RANGE);
			}
		}
		
		return String.format("DEFAULT '%s'", TIMESTAMP_BOTTOM_RANGE);
	}
	
}
