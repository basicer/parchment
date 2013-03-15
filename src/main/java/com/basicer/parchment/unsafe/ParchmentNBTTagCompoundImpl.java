package com.basicer.parchment.unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.bukkit.entity.Player;

import com.basicer.parchment.Debug;

public class ParchmentNBTTagCompoundImpl  {



	public static ParchmentNBTTagCompound getTag(Object p, boolean set_if_empty) {
		System.err.println("Okah So, type of p is " + p.getClass().getName());
		Field fhandle;
		Method tagGetter;
		Method tagSetter;
		try {
			fhandle = p.getClass().getDeclaredField("handle");
			fhandle.setAccessible(true);
			Object o = fhandle.get(p);
			if ( o == null ) {
				Debug.trace("Bucket object has no handle: " + p.getClass());
				return null;
			}
			tagGetter = o.getClass().getMethod("getTag");
			Object otag = tagGetter.invoke(o);

			/*
			if (otag == null && set_if_empty) {
				tagSetter = o.getClass().getMethod("setTag", NBTTagCompound.class);
				System.out.println("Tagsetter: " + tagSetter);
				tagSetter.invoke(o, new NBTTagCompound());
			}*/

			if (otag == null)
				return null;

			return ProxyFactory.createProxy(ParchmentNBTTagCompound.class, otag);

			
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

	}

}
