package com.qorporation.qluster.conn.sql.streamer.component;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.sql.streamer.SQLPrimaryKeyStreamer;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Column;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.annotation.AllowNull;
import com.qorporation.qluster.util.ErrorControl;
import com.qorporation.qluster.util.Serialization;

public class StringStreamer extends SQLPrimaryKeyStreamer<String> {

	public StringStreamer(EntityService service, Class<String> type,
			ParameterizedType subType) {
		super(service, type, subType);
	}

	@Override
	public String write(Object comp, byte[] name, long ts) {
		return (String) comp;
	}

	@Override
	public String read(Object comp) {
		if (comp.getClass().equals(String.class)) {
			return (String) comp;
		} else {
			return Serialization.deserializeString((byte[]) comp);
		}
	}

	@Override
	public String generateKey() {
		ErrorControl.fatal(String.format("Attempted to use %s to generate key", this.type.getName()));
		return null;
	}
	
	@Override
	public String generateColumnSchemaType(Field field) {
		Column c = field.getAnnotation(Column.class);
		if (c != null) {
			if (c.length() > -1) {
				if (c.length() == 0) {
					if (c.caseSensitive()) {
						return "BLOB DEFAULT ''";
					} else {
						return "TEXT DEFAULT ''";
					}
				} else {
					if (c.caseSensitive()) {
						return String.format("VARBINARY(%s)", c.length());
					} else {
						return String.format("VARCHAR(%s)", c.length());
					}
				}
			}
		}
		
		return "VARCHAR(255)";
	}
	
	@Override
	public String generateColumnSchemaDefaultsAndConstraints(Field field) {
		return field.getAnnotation(AllowNull.class) == null ? "DEFAULT ''" : "";
	}

	@Override
	public String generateColumnSchemaPrimaryKeyModifier(Field field) {
		ErrorControl.fatal(String.format("Attempted to use %s as primary key", this.type.getName()));
		return null;
	}

}
