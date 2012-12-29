package com.basicer.parchment;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context.ParameterPtr;
import com.basicer.parchment.parameters.*;

public abstract class Spell extends TCLCommand {
	
	public enum DefaultTargetType { None, Self, TargetBlock, TargetPlace };
	public enum FirstParamaterTargetType { Never, ExactMatch, FuzzyMatch };
	
	public String getName() { return this.getClass().getSimpleName(); }
	
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.ExactMatch;
	}
	
	public DefaultTargetType getDefaultTargetType(Context ctx) { 
		String source = ctx.getSource();
		System.out.println("GDT " + this.getName());
		if ( source == null || source.equals("command") || true ) {
			//TODO: Caster might not be player, so this doesnt make much sense.
			if ( this.canAffect(PlayerParameter.class) ) {
				System.out.println(this.getClass().getName() + " => Self");
				return DefaultTargetType.Self;
			} else if ( this.canAffect(ItemParameter.class) ) {
				System.out.println(this.getClass().getName() + " => Self");
				return DefaultTargetType.Self;
			} else if ( this.canAffect(BlockParameter.class) ) {
				System.out.println(this.getClass().getName() + " => TargetBlock");
				return DefaultTargetType.TargetBlock;
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
		if ( params.length > 1 ) {
			Parameter test = params[1];
			if ( test instanceof ListParameter) {
				if ( test.getHomogeniousType() != null ) {
					test = ((ListParameter)test).get(0);
					System.out.println("Casted list type");
				}
			}
			if ( test != null ) {
				switch ( getFirstParamaterTargetType(ctx) ) {
					case Never:
						test = null;
						break;
					case ExactMatch:
						if ( !this.canAffect(test.getClass()) ) {
							test = null;
						}
						break;
					case FuzzyMatch:
						List<Class<? extends Parameter>> list = getAffectors();
						boolean match = false;
						for ( Class<? extends Parameter> c : list ) {
							if ( test.cast(c, ctx) != null ) {
								match = true;
								break;
							} else {
								System.out.println("FAIL MATCH " + c.getSimpleName() + " to " + test.getClass().getSimpleName());
							}
						}
						
						if ( !match ) test = null;
						break;
						
				}
				
				if ( test != null ) {
					System.out.println("Casting first param to target for " + this.getClass().getSimpleName());
					Parameter[] nparams = new Parameter[params.length - 1];
					System.out.println("C " + params.length);
					System.out.println("X " + nparams.length);
					nparams[0] = params[0];
					System.arraycopy(params, 2, nparams, 1, nparams.length - 1);
					ctx.setTarget(params[1]);
					params = nparams;
					
				}
			}
		}
		// TODO Auto-generated method stub
		return super.bindContext(params, spellstatic);
	}

	public Parameter execute(Context ctx) {
		try {
			Parameter targets = ctx.getTarget();
			System.out.println("EXEC " + getName() + " with target " + 
					(targets == null ? "null" : targets.toString()));
			return this.cast(ctx);
		} catch ( FizzleException fizzle ) {
			ctx.sendDebugMessage("The spell fizzles: " + fizzle.getMessage());
			return Parameter.from("fizzle");
		}
	}
	
	private List<Class<? extends Parameter>> getAffectors() {
		List<Class<? extends Parameter>> list = new ArrayList<Class<? extends Parameter>>();
		for ( Method m : this.getClass().getMethods() ) {
			if ( m.getName() != "affect" ) continue;
			Class[] types = m.getParameterTypes();
			list.add(types[0]);
		}
		
		return list;
	}
	
	public Parameter cast(Context ctx) {
		return Spell.defaultCastBehavior(this, ctx);
	}
		
	protected static Parameter defaultCastBehavior(Spell s, Context ctx) {
		List<Class<? extends Parameter>> list = s.getAffectors();
		
		System.out.println("TI " + s.getName() + " with target " + 
				(ctx.getTarget() == null ? "null" : ctx.getTarget().toString()));
		Parameter targets = s.resolveTarget(ctx);
		System.out.println("TR " + s.getName() + " coming out as " + 
				(targets == null ? "null" : targets.toString()));
		ArrayList<Parameter> out = new ArrayList<Parameter>();
		if ( targets == null ) s.fizzle();
		ParameterPtr result;
		targetloop:
		for ( Parameter t : targets ) {
			for ( Class<? extends Parameter> c : list ) {
				result = s.tryAffect(c, t, ctx);
				if ( result != null ) {
					out.add(result.val);
					continue targetloop;
				}
			}
			
			for ( Class<? extends Parameter> c : list ) {
				Parameter casted = t.cast(c, ctx);
				ctx.sendDebugMessage(c.getName() + "?" + (casted == null ? "Null" : "Pass"));
				if ( casted == null ) continue;
				result = s.tryAffect(c, casted, ctx);
				if ( result != null ) {
					out.add(result.val);
					continue targetloop;
				}
				
			}

		}
		
		if ( out.size() == 0 ) return null;
		else if ( out.size() == 1 ) return out.get(0);
		else return Parameter.createList(out.toArray(new Parameter[0]));
		
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
		List<Block> sight = null;
		switch ( this.getDefaultTargetType(ctx) ) {
			case None:
				break;
			case Self:
				t = ctx.getCaster();
				break;
			case TargetBlock:
				if ( casterp == null ) return null;
				sight = casterp.getLastTwoTargetBlocks(null, 100);
				if ( sight.size() < 2 ) return Parameter.from(casterp.getTargetBlock(null, 100));
				return Parameter.from(sight.get(1), sight.get(1).getFace(sight.get(0)));				
			case TargetPlace:
				if ( casterp == null ) return null;
				sight = casterp.getLastTwoTargetBlocks(null, 100);
				if ( sight.size() < 2 ) return null;
				return Parameter.from(sight.get(0), sight.get(0).getFace(sight.get(1)));
		}
		
		
		if ( t != null ) ctx.setTarget(t);
		return t;
	}
	
	private boolean canAffect(Class<? extends Parameter> type) {
		return getAffectors().contains(type);
	}

	
	private <T extends Parameter> ParameterPtr tryAffect(Class<T> type, Parameter t, Context ctx) {
		if ( !type.isInstance(t) ) return null;
		if ( !this.canAffect(type) ) return null;
		Class[] types = new Class[] { type, Context.class };
		try {
			System.out.println("INVOKE " + t.getClass() + " bread inside " + this.getClass().getSimpleName());
			Method m = this.getClass().getMethod("affect", types);
			Parameter p = (Parameter) m.invoke(this, t, ctx);
			ParameterPtr o = new ParameterPtr();
			o.val = p;
			return o;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			if ( e.getTargetException() instanceof RuntimeException )
				throw (RuntimeException) e.getTargetException();
			if ( e.getTargetException() instanceof Error )
				throw (Error)e.getTargetException();
			
			e.printStackTrace();
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