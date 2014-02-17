package com.basicer.parchment;

import org.apache.http.MethodNotSupportedException;

import java.lang.reflect.*;

/**
 * Created by basicer on 2/16/14.
 */
public class Reflect {
	public static Field getField(Object o, String name) {
		Class clazz = o.getClass();
		Field f = null;
		try {
			f = clazz.getDeclaredField(name);
		} catch (NoSuchFieldException e) {
			return null;
		}
		return f;
	}

	public static <T> T getFieldValue(Object o, Class<T> type, String name) {
		Field f = getField(o, name);
		try {
			f.setAccessible(true);
			return (T) f.get(o);
		} catch (IllegalAccessException e) {
			throw new FizzleException(e.getMessage());
		}
	}

	public static <T> T invokeMethod(Object o, Class<T> returnType, String name, Object ... params) {
		Class clazz = o.getClass();

		Method target = null;
		for ( Method m : clazz.getMethods() ) {
			if ( !m.getName().equals(name) ) continue;
			Class<?>[] types = m.getParameterTypes();
			if ( types.length != params.length ) continue;
			if ( !returnType.isAssignableFrom((m.getReturnType())) ) continue;
			for ( int i = 0; i < params.length; ++i ) {
				if ( ! (types[i].isAssignableFrom(params[i].getClass())) ) continue;
			}
			target = m;
			break;
		}

		if ( target == null ) throw new FizzleException("Couldn't find required method: " + name + " on " + o.getClass().getName());

		try {
			Object out = target.invoke(o, params);
			return (T)out;
		} catch (IllegalAccessException e) {
			throw new FizzleException(e.getMessage());
		} catch (InvocationTargetException e) {
			throw new FizzleException(e.getTargetException().getMessage());
		}
	}
}
