package com.basicer.parchment;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.parameters.*;

public abstract class Spell  {
	
		
	public void cast(Context ctx) {
		List<Class<? extends Parameter>> list = new ArrayList<Class<? extends Parameter>>();
		for ( Type c : this.getClass().getGenericInterfaces() ) {
			ParameterizedType pt = (ParameterizedType) c;
			Class base = (Class)pt.getRawType();
			Type[] tx = pt.getActualTypeArguments();
			if ( base != Affectable.class ) continue;
			list.add((Class<? extends Parameter>)tx[0]);
 		}
		
		for ( Parameter t : resolveTarget(ctx) ) {
			for ( Class<? extends Parameter> c : list )
			if ( this.tryAffect(c, t, ctx) ) break;
			
		}
	}
	
	protected void fizzle() {
		throw new FizzleException();
	}
	
	protected Parameter resolveTarget(Context ctx) {
		Parameter t = ctx.getTarget();
		if ( t != null ) return t;
		
		return ctx.getCaster();
	}
	
	private <T extends Parameter> boolean tryAffect(Class<T> type, Parameter t, Context ctx) {
		if ( !type.isInstance(t) ) return false;
		T x = (T) t;
		if ( this instanceof Affectable<?> ) {
			((Affectable<T>) this).affect(x, ctx);
			return true;
		}
		
		return false;
	}
	
	
	public class FizzleException extends RuntimeException { }

}
