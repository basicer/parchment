package com.basicer.parchment.unsafe;

import java.lang.annotation.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Retention(RetentionPolicy.RUNTIME)
@interface Unwrap {
	
}

interface ProxyInterface {
	Object unproxy();
}

public class ProxyFactory {
	@SuppressWarnings("unchecked")
	public static <T> T createProxy(final Class<T> type, final Object base) {
		if ( base == null ) return null;
		return (T) Proxy.newProxyInstance(type.getClassLoader(),
				new Class[] { type }, new InvocationHandler() {
					public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
						Method impl = null;
						if ( method.getName().equals("unproxy") ) {
							return base;
						}
						
						//TODO: Memorize this
						Class[] target_types = method.getParameterTypes();
						

						methodsearch:
						for ( Method m : base.getClass().getMethods() ) {
							if ( !m.getName().equals(method.getName()) ) continue;
							Class[] canidate_types = m.getParameterTypes();
							if ( target_types.length != canidate_types.length ) continue;
							for ( int i = 0; i < target_types.length; ++i ) {
								if ( method.getAnnotation(Unwrap.class) != null ) {
									Object base = ((ProxyInterface) args[i]).unproxy();
									if ( !canidate_types[i].isInstance(base) ) continue methodsearch;
								} else {
									if ( target_types[i] != canidate_types[i] ) continue methodsearch;
								}
							}
							impl = m;
							break;
						}
						
						for ( int i = 0; i < target_types.length; ++i ) {
							if ( method.getAnnotation(Unwrap.class) != null ) {
								args[i] = ((ProxyInterface) args[i]).unproxy();
							}
						}
						
						
						if ( impl == null ) {
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
