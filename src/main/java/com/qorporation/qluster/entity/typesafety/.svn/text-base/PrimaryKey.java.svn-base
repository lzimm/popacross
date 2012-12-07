package com.qorporation.qluster.entity.typesafety;

import java.lang.reflect.ParameterizedType;

import com.qorporation.qluster.entity.Definition;
import com.qorporation.qluster.entity.typesafety.PrimaryKeyType;

public class PrimaryKey<T, P extends PrimaryKeyType<T>> extends Key<T> {

	public PrimaryKey(Class<? extends Definition<?>> parent, Class<T> type, ParameterizedType subType, String name) {
		super(parent, type, subType, name);
	}

}
