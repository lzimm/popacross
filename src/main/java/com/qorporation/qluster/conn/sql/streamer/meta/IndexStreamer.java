package com.qorporation.qluster.conn.sql.streamer.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.qorporation.qluster.conn.sql.streamer.SQLMetaKeyStreamer;
import com.qorporation.qluster.conn.sql.typesafety.SQLBackedDefinition;
import com.qorporation.qluster.conn.sql.typesafety.keytypes.annotation.Indexed;
import com.qorporation.qluster.entity.EntityService;
import com.qorporation.qluster.entity.typesafety.IndexMetaKey;

public class IndexStreamer extends SQLMetaKeyStreamer<IndexMetaKey> {

	public IndexStreamer(EntityService service) {
		super(service);
	}

	@Override
	public String generateSchema(Class<? extends SQLBackedDefinition> definition, Field field) {
		StringBuilder builder = new StringBuilder();
		
		for (Annotation a: field.getAnnotations()) {			
			if (a.annotationType().equals(Indexed.class)) {
				StringBuilder fieldBuilder = new StringBuilder();
				for (String f: ((Indexed) a).fields()) {
					if (fieldBuilder.length() > 0) fieldBuilder.append(',');
					fieldBuilder.append("`").append(f).append("`");
				}
				
				if (((Indexed) a).unique()) {
					builder.append(String.format("ALTER TABLE `%s` ADD UNIQUE KEY `unique_%s` (%s);\n",
							definition.getSimpleName().toLowerCase(), 
							field.getName(),
							fieldBuilder.toString()));
				} else {
					builder.append(String.format("ALTER TABLE `%s` ADD INDEX `index_%s` (%s);\n",
							definition.getSimpleName().toLowerCase(), 
							field.getName(),
							fieldBuilder.toString()));
				}
			}
		}
		
		return builder.toString();
	}

}
