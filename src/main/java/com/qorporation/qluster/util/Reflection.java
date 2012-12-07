package com.qorporation.qluster.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class Reflection {
	
	public static Class<?> getParamType(Field field, int i) {
		return getParamType(field.getGenericType(), i);
	}

	public static Class<?> getParamType(Class<?> cls, int i) {
		return getParamType(cls.getGenericSuperclass(), i);
	}
	
	public static Class<?> getParamType(Type type, int i) {
		if (type.equals(Object.class)) return Object.class;
		ParameterizedType aType = (ParameterizedType) type;
		Object typeArg = aType.getActualTypeArguments()[i];
		if (typeArg.getClass().equals(Class.class)) {
			return (Class<?>) typeArg;
		} else if (ParameterizedType.class.isAssignableFrom(typeArg.getClass())) {
			ParameterizedType subType = (ParameterizedType) typeArg;
			return (Class<?>) subType.getRawType();
		} else if (TypeVariable.class.isAssignableFrom(typeArg.getClass())) {
			TypeVariable<?> subType = (TypeVariable<?>) typeArg;
			Type firstBound = subType.getBounds()[0];
			if (firstBound.getClass().equals(Class.class)) {
				return (Class<?>) firstBound;
			} else if (ParameterizedType.class.isAssignableFrom(firstBound.getClass())) {
				ParameterizedType firstBoundSubType = (ParameterizedType) firstBound;
				return (Class<?>) firstBoundSubType.getRawType();				
			}
		}
		
		return null;
	}
	
	public static ParameterizedType getParamSubType(Field field, int i) {
		return getParamSubType(field.getGenericType(), i);
	}
	
	public static ParameterizedType getParamSubType(Class<?> cls, int i) {
		return getParamSubType(cls.getGenericSuperclass(), i);
	}
	
	public static ParameterizedType getParamSubType(Type type, int i) {
		try {
			ParameterizedType aType = (ParameterizedType) type;
			Object typeArg = aType.getActualTypeArguments()[i];
			return (ParameterizedType) typeArg;
		} catch (Exception e) {
			return null;
		}
	}

}
