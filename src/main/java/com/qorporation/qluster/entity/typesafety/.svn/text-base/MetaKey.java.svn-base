package com.qorporation.qluster.entity.typesafety;

import java.lang.annotation.Annotation;

import com.qorporation.qluster.entity.Definition;

public class MetaKey {

	protected Class<? extends Definition<?>> parent = null;
	protected String name = null;
	protected Annotation[] annotations = null;
	
	public MetaKey(Class<? extends Definition<?>> parent, String name, Annotation[] annotations) {
		this.parent = parent;
		this.name = name;
		this.annotations = annotations;
	}
	
	public Class<? extends Definition<?>> getParent() { return this.parent; }
	public String getName() { return this.name; }
	public Annotation[] getAnnotations() { return this.annotations; }
	
}
