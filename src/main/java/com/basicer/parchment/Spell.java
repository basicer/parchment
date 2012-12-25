package com.basicer.parchment;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context.ParameterPtr;
import com.basicer.parchment.parameters.*;

public abstract class Spell extends TCLCommand {
	
	public enum DefaultTargetType { None, Self, TargetBlock };
	
	public DefaultTargetType getDefaultTargetType(Context ctx) { 
		String source = ctx.getSource();
		if ( source == null || source.equals("command") ) {
			//TODO: Caster might not be player, so this doesnt make much sense.
			if ( this.canAffect(PlayerParameter.class) ) {
				System.out.println(this.getClass().getName() + " => Self");
				return DefaultTargetType.Self;
			}
			System.out.println(this.getClass().getName() + " => None");
			return DefaultTargetType.None;
		}
		System.out.println(this.getClass().getName() + " => None");
		return DefaultTargetType.None; 
	}
	


	protected Context spellStatic;
	
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
			return this.cast(ctx);
		} catch ( FizzleException fizzle ) {
			ctx.sendDebugMessage("The spell fizzles: " + fizzle.getMessage());
			return Parameter.from("fizzle");
		}
	}
	
	public Parameter cast(Context ctx) {
		List<Class<? extends Parameter>> list = new ArrayList<Class<? extends Parameter>>();
		for ( Type c : this.getClass().getGenericInterfaces() ) {
			ParameterizedType pt = (ParameterizedType) c;
			Class base = (Class)pt.getRawType();
			Type[] tx = pt.getActualTypeArguments();
			if ( base != Affectable.class ) continue;
			list.add((Class<? extends Parameter>)tx[0]);
 		}
		
		Parameter targets = resolveTarget(ctx);
		ArrayList<Parameter> out = new ArrayList<Parameter>();
		if ( targets == null ) fizzle();
		ParameterPtr result;
		targetloop:
		for ( Parameter t : targets ) {
			ctx.sendDebugMessage("TARGET> " + t.toString());
			for ( Class<? extends Parameter> c : list ) {
				result = this.tryAffect(c, t, ctx);
				if ( result != null ) {
					out.add(result.val);
					continue targetloop;
				}
				
			}
			
			for ( Class<? extends Parameter> c : list ) {
				Parameter casted = t.cast(c, ctx);
				ctx.sendDebugMessage(c.getName() + "?" + (casted == null ? "Null" : "Pass"));
				if ( casted == null ) continue;
				result = this.tryAffect(c, casted, ctx);
				if ( result != null ) {
					out.add(result.val);
					continue targetloop;
				}
				
			}

		}
		
		return Parameter.createList(out.toArray(new Parameter[0]));
		
	}
	
	protected void fizzle() {
		throw new FizzleException();
	}
	
	protected void fizzle(String why) {
		throw new FizzleException(why);
	}
	
	protected void fizzleTarget(String string) {
		fizzle("Invalid target: " + string);
		
	}
	
	protected <T extends Parameter> T getArgOrFizzle(Context ctx, int arg, Class<T> type) {
		ArrayList<Parameter> args = ctx.getArgs();
		if ( args.size() <= arg ) fizzle("Not enough arguements.");
		Parameter t = args.get(arg);
		if ( t == null ) fizzle("Not enough arguments.");
		T tt = t.cast(type);
		if ( tt == null ) fizzle("Could not convert argument " + arg + " to " + type.toString());
		return tt;
	}
	
	protected <T extends Parameter> T getArg(Context ctx, int arg, Class<T> type) {
		ArrayList<Parameter> args = ctx.getArgs();
		if ( args.size() <= arg ) return null;
		Parameter t = args.get(arg);
		if ( t == null ) return null;
		T tt = t.cast(type);
		if ( tt == null ) return null;
		return tt;
	}
	
	protected Parameter resolveTarget(Context ctx) {
		Parameter t = ctx.getTarget();
		if ( t != null ) return t;
		LivingEntity casterp = ctx.getCaster().asLivingEntity();
		switch ( this.getDefaultTargetType(ctx) ) {
			case None:
				break;
			case Self:
				t = ctx.getCaster();
				break;
			case TargetBlock:
				if ( casterp == null ) return null;
				return Parameter.from(casterp.getTargetBlock(null, 100));
		}
		
		
		if ( t != null ) ctx.setTarget(t);
		return t;
	}
	
	private boolean canAffect(Class<? extends Parameter> type) {
		for ( Type c : this.getClass().getGenericInterfaces() ) {
			ParameterizedType pt = (ParameterizedType) c;
			Class base = (Class)pt.getRawType();
			Type[] tx = pt.getActualTypeArguments();
			if ( base != Affectable.class ) continue;
			if ( type == tx[0] ) return true;
 		}
		return false;
	}

	
	private <T extends Parameter> ParameterPtr tryAffect(Class<T> type, Parameter t, Context ctx) {
		if ( !type.isInstance(t) ) return null;
		if ( !this.canAffect(type) ) return null;
		T x = (T) t;
		if ( this instanceof Affectable<?> ) {
			Parameter out = ((Affectable<T>) this).affect(x, ctx);
			return new ParameterPtr(out);
		}
		
		return null;
	}
	
	
	public class FizzleException extends RuntimeException {

		private static final long	serialVersionUID	= 4163289662961586743L;

		public FizzleException(String why) {
			super(why);
		} 

		public FizzleException() {
			super();
		} 
	}
}