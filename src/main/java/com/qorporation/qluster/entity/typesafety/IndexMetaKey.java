package com.qorporation.qluster.entity.typesafety;

import java.lang.annotation.Annotation;

import com.qorporation.qluster.entity.Definition;

public class IndexMetaKey extends MetaKey {

	public IndexMetaKey(Class<? extends Definition<?>> parent, String name,
			Annotation[] annotations) {
		super(parent, name, annotations);
	}

}
