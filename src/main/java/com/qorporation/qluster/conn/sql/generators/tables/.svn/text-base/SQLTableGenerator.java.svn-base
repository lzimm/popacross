package com.qorporation.qluster.conn.sql.generators.tables;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.conn.sql.generators.SQLSchemaTableGenerator;
import com.qorporation.qluster.conn.sql.streamer.SQLComponentStreamer;
import com.qorporation.qluster.conn.sql.streamer.SQLMetaKeyStreamer;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.conn.sql.typesafety.SQLTable;
import com.qorporation.qluster.entity.typesafety.MetaKey;
import com.qorporation.qluster.entity.typesafety.PrimaryKey;
import com.qorporation.qluster.util.Reflection;

public class SQLTableGenerator extends SQLSchemaTableGenerator<SQLTable> {

	@Override
	protected void generate(Class<? extends SQLBackedDefinition> definition, PrintStream out) {
		out.println(String.format("\n\n\nCREATE TABLE `%s` (", definition.getSimpleName().toLowerCase()));

		for (Field f: definition.getFields()) {
			if (MetaKey.class.isAssignableFrom(f.getType())) continue;

			if (ParameterizedType.class.isAssignableFrom(f.getGenericType().getClass())) {
				ParameterizedType field = (ParameterizedType) f.getGenericType();
				if (field.getRawType().equals(PrimaryKey.class)) {
					Class<?> fieldType = Reflection.getParamType(field, 0);
					ParameterizedType fieldTypeParam = Reflection.getParamSubType(field, 0);
					
					SQLComponentStreamer<?> streamer = this.getStreamer(fieldType, fieldTypeParam);
					out.println(streamer.generateColumnSchema(definition, f));			
				}
			}
		}
		
		out.println(String.format(") ENGINE=MyISAM DEFAULT CHARSET=latin1;\n"));
		
		for (Field f: definition.getFields()) {
			if (MetaKey.class.isAssignableFrom(f.getType())) {
				SQLMetaKeyStreamer<?> streamer = this.getMetaStreamer(f.getType());
				out.println(streamer.generateSchema(definition, f));
			} else if (ParameterizedType.class.isAssignableFrom(f.getGenericType().getClass())) {
				ParameterizedType field = (ParameterizedType) f.getGenericType();
				if (field.getRawType().equals(PrimaryKey.class)) continue;
				
				Class<?> fieldType = Reflection.getParamType(field, 0);
				ParameterizedType fieldTypeParam = Reflection.getParamSubType(field, 0);
				
				SQLComponentStreamer<?> streamer = this.getStreamer(fieldType, fieldTypeParam);
				
				out.println(streamer.generateColumnSchema(definition, f));
				out.print(streamer.generateAnnotationSchema(definition, f));
			}
		}
	}

}
