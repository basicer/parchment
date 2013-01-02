package com.basicer.parchment.unsafe;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyFactory {
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(final Class<T> type, final Object base) {
		return (T) Proxy.newProxyInstance(type.getClassLoader(),
				new Class[] { type }, new InvocationHandler() {
					public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
						Method impl = null;
						if ( method.getName().equals("unproxy") ) {
							return base;
						}
						try {
							impl = base.getClass().getMethod(method.getName(), method.getParameterTypes());
						} catch ( java.lang.NoSuchMethodException ex ) {
							throw new Exception("Ducktype assertion failed, " + base.getClass().getName() + " can't be a " + type.getName() +
								" because it has no method " + method.getName() );
						}
						try {
							Object result = impl.invoke(base, args);
							Class<?> rt = method.getReturnType();
							if ( rt.isPrimitive()) return result;
							else if ( rt.isInstance(result) ) {
								return result;
							} else {
								//System.out.println("Need " + method.getReturnType().toString() + " but have " + result.getClass().toString());
								return createProxy(method.getReturnType(), result);
							}
						} catch ( java.lang.reflect.InvocationTargetException ex ) {
							throw ex.getTargetException();
						}
						
					}
			
		});
	}

}
