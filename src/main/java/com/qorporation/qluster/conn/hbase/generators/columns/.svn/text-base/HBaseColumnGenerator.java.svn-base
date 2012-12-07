package com.qorporation.qluster.conn.hbase.generators.columns;

import java.lang.reflect.ParameterizedType;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.qorporation.qluster.conn.hbase.generators.HBaseSchemaColumnGenerator;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseBackedDefinition;
import com.qorporation.qluster.conn.hbase.typesafety.HBaseColumn;
import com.qorporation.qluster.entity.typesafety.FieldKey;

public class HBaseColumnGenerator extends HBaseSchemaColumnGenerator<HBaseColumn<?, ?>> {

	@Override
	public void generate(Class<? extends HBaseBackedDefinition> def, ParameterizedType definitionType, Document xmldoc, Element column) {
		for(FieldKey<?> k: this.entityService.getManager(def).getKeys()) {
			Element field = xmldoc.createElement("Column");
			field.setAttribute("Name", k.getName());
			column.appendChild(field);
		}
	}

}
