package com.basicer.parchment;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.parameters.*;

public abstract class Spell extends TCLCommand {
	
	public enum DefaultTargetType { None, Self };
	public DefaultTargetType getDefaultTargetType() { return DefaultTargetType.None; }
	
	private Context spellStatic;
	
	public Spell() {
		spellStatic = new Context();
	}
	
	@Override
	public Context bindContext(Parameter[] params, Context ctx) {
		Context spellstatic = ctx.createBoundSubContext(spellStatic);
		
		// TODO Auto-generated method stub
		return super.bindContext(params, spellstatic);
	}



	public Parameter execute(Context ctx) {
		try {
			this.cast(ctx);
			return Parameter.from("success");
		} catch ( FizzleException fizzle ) {
			ctx.sendDebugMessage("The spell fizzles");
			return Parameter.from("fizzle");
		}
	}
	
	public void cast(Context ctx) {
		List<Class<? extends Parameter>> list = new ArrayList<Class<? extends Parameter>>();
		for ( Type c : this.getClass().getGenericInterfaces() ) {
			ParameterizedType pt = (ParameterizedType) c;
			Class base = (Class)pt.getRawType();
			Type[] tx = pt.getActualTypeArguments();
			if ( base != Affectable.class ) continue;
			list.add((Class<? extends Parameter>)tx[0]);
 		}
		
		Parameter targets = resolveTarget(ctx);
		
		targetloop:
		for ( Parameter t : targets ) {
			ctx.sendDebugMessage("TARGET> " + t.toString());
			for ( Class<? extends Parameter> c : list ) {
				if ( this.tryAffect(c, t, ctx) ) continue targetloop;
			}
			
			for ( Class<? extends Parameter> c : list ) {
				Parameter casted = t.cast(c);
				ctx.sendDebugMessage(c.getName() + "?" + casted == null ? "Null" : "Pass");
				if ( casted == null ) continue;
				if ( this.tryAffect(c, casted, ctx) ) continue targetloop;
			}

		}
		
		
		
		
	}
	
	protected void fizzle() {
		throw new FizzleException();
	}
	
	protected Parameter resolveTarget(Context ctx) {
		Parameter t = ctx.getTarget();
		if ( t != null ) return t;
		switch ( this.getDefaultTargetType() ) {
			case None:
				break;
			case Self:
				t = ctx.getCaster();
				break;
		}
		
		if ( t != null ) ctx.setTarget(t);
		return t;
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
