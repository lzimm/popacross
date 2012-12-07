package com.qorporation.qluster.conn.sql.streamer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;

public abstract class SQLPrimaryKeyStreamer<T> extends SQLComponentStreamer<T> {
	
	public SQLPrimaryKeyStreamer(EntityService service, Class<T> type, ParameterizedType subType) {
		super(service, type, subType);
	}
	
	public String asString(Object comp) {
		return comp.toString();
	}
	
	public abstract String generateKey();

	@Override
	public String generateColumnSchema(Class<? extends SQLBackedDefinition> definition, Field field) {
		ParameterizedType type = (ParameterizedType) field.getGenericType();
		if (type.getRawType().equals(PrimaryKey.class)) {
			return String.format("`%s` %s NOT NULL %s", 
					field.getName(),
					generateColumnSchemaType(field),
					generateColumnSchemaPrimaryKeyModifier(field));
		} else {
			return super.generateColumnSchema(definition, field);
		}
	}
	
	public abstract String generateColumnSchemaPrimaryKeyModifier(Field field);
	
}
