package com.basicer.parchment;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;


import com.basicer.parchment.Context.ParameterPtr;
import com.basicer.parchment.parameters.*;

public abstract class TargetedCommand extends TCLCommand {
	
	public enum DefaultTargetType { None, Self, TargetBlock, TargetPlace, World, Server };
	public enum FirstParameterTargetType { Never, ExactMatch, FuzzyMatch, Always };
	
	
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.ExactMatch;
	}
	
	public DefaultTargetType getDefaultTargetType(Context ctx, String source) { 
		Debug.trace("GDT " + this.getName());
		if ( source == null || source.equals("command") || true ) {
			//TODO: Caster might not be player, so this doesnt make much sense.
			if ( this.canAffect(PlayerParameter.class) ) {
				Debug.trace(this.getClass().getName() + " => Self");
				return DefaultTargetType.Self;
			} else if ( this.canAffect(ItemParameter.class) ) {
				Debug.trace(this.getClass().getName() + " => Self");
				return DefaultTargetType.Self;
			} else if ( this.canAffect(BlockParameter.class) ) {
				Debug.trace(this.getClass().getName() + " => TargetBlock");
				return DefaultTargetType.TargetBlock;
			}  else if ( this.canAffect(ServerParameter.class) ) {
				return DefaultTargetType.Server;
			}
			Debug.trace(this.getClass().getName() + " => None");
			return DefaultTargetType.None;
		}
		Debug.trace(this.getClass().getName() + " => None");
		return DefaultTargetType.None; 
	}
	


	protected Context spellStatic;
	
	public TargetedCommand() {
		spellStatic = new Context();
	}
	
	@Override
	public Context bindContext(Parameter[] params, Context ctxi) {
		FirstParameterTargetType fpt = getFirstParameterTargetType(ctxi);
		Parameter targetOveride = null;
		boolean force = false;
		int off = 1;
		if ( params.length > 1 ) {
			Parameter test = params[1];
			if ( test instanceof StringParameter && ( test.asString(ctxi).equals("-target") || test.asString(ctxi).equals("-@") )) {
				off = 2;
				if ( params.length < 3 ) throw new FizzleException("Specify a target when using -@");
				test = params[2];
				force = true;
				fpt = FirstParameterTargetType.Always;
			}
			Parameter target = test;
			if ( test instanceof ListParameter) {
				if ( test.getHomogeniousType() != null ) {
					test = ((ListParameter)test).index(0);
					Debug.trace("Casted list type");
				} else {
					Debug.trace("List is not homogenious");
				}
			}
			if ( test != null ) {
				switch ( fpt ) {
					case Never:
						test = null;
						break;
					case Always:
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
							Parameter casted = test.cast(c, ctxi);
							if ( casted != null ) {
								match = true;
								break;
							} else {
								Debug.trace("FAIL MATCH " + c.getSimpleName() + " to " + test.getClass().getSimpleName());
							}
						}

						if ( !match ) test = null;
						break;
						
				}
				
				if ( test != null || force ) {
					
					Debug.trace("Casting first param to target for " + this.getClass().getSimpleName());
					Debug.trace("Target now: " + target.toString());
					Parameter[] nparams = new Parameter[params.length - off];
					Debug.trace("C " + params.length);
					Debug.trace("X " + nparams.length);
					nparams[0] = params[0];
					System.arraycopy(params, off + 1, nparams, 1, params.length - off - 1);
					targetOveride = target;
					params = nparams;
					
				}
			}
		}

		//
		Context out = super.bindContext(params, ctxi);
		if ( targetOveride != null || force ) out.setTarget(targetOveride);
		out.put("this", Parameter.from(this));
		return out;
	}

	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return executeBinding("cast", ctx, e);
	}
	
	
	public EvaluationResult executeBinding(String name, final Context ctx, final TCLEngine engine) {
		try {
			Parameter targets = ctx.getTarget();
			Debug.trace("EXEC " + getName() + ":" + name + " with target " + 
					(targets == null ? "null" : targets.toString()));
			if ( !name.equals("cast") ) fizzle("Can only use cast binding on this spell.");
			return EvaluationResult.makeOkay(this.cast(ctx));
		} catch ( FizzleException fizzle ) {
			//ctx.sendDebugMessage("The spell fizzles: " + fizzle.getMessage());
			return EvaluationResult.makeError(fizzle.getMessage());
		}
	}
	
	protected List<Class<? extends Parameter>> getAffectors() {
		List<Class<? extends Parameter>> list = new ArrayList<Class<? extends Parameter>>();
		for ( Method m : this.getClass().getMethods() ) {
			if ( m.getName().equals("affectNull") )  {
				list.add(null);
				continue;
			} else if ( !m.getName().equals("affect") ) continue;
			Class[] types = m.getParameterTypes();
			list.add(types[0]);
		}



		return list;
	}
	
	public Parameter cast(Context ctx) {
		//By default a casted spell applies it affector to the target.
		return TargetedCommand.applyAffectors(this, ctx);
	}
	
	protected static Parameter applyAffectors(TargetedCommand s, Context ctx) {
		List<Class<? extends Parameter>> list = s.getAffectors();
		
		Debug.trace("TI " + s.getName() + " with target " + 
				(ctx.getTarget() == null ? "null" : ctx.getTarget().toString()));
		Parameter targets = s.resolveTarget(ctx);
		Debug.trace("TR " + s.getName() + " coming out as " + 
				(targets == null ? "null" : targets.toString()));
		
		//TODO : Do we want to collapse duplicate returns by default ?
		Collection<Parameter> out = null;
		if ( s.getShouldCombindDuplicateListOutput() ) {
			out = new HashSet<Parameter>();
		} else {
			out = new ArrayList<Parameter>();
		}

		ParameterPtr result;
		boolean did_something = false;
		if ( targets == null ) {
			result = s.tryAffect(null, null, ctx);
			if ( result != null ) {
				did_something = true;
				out.add(result.getValue());
			}
		} else {
			Parameter save = ctx.get("target");


			targetloop:
			for ( Parameter t : targets ) {
				for ( Class<? extends Parameter> c : list ) {
					ctx.setTarget(t);
					result = s.tryAffect(c, t, ctx);
					if ( result != null ) {
						did_something = true;
						// Combine returned lists into one large list.
						if ( result.getValue() != null ) {
							for ( Parameter rr : result.getValue() ) {
								out.add(rr);
							}
						}
						continue targetloop;
					}
				}

				for ( Class<? extends Parameter> c : list ) {
					Parameter casted = t.cast(c, ctx);
					if ( casted == null ) continue;
					ctx.setTarget(casted);
					result = s.tryAffect(c, casted, ctx);
					if ( result != null ) {
						did_something = true;
						// Combine returned lists into one large list.
						for ( Parameter rr : result.getValue() ) {
							out.add(rr);
						}
						continue targetloop;
					}
				}
			}

			if ( save != null ) ctx.put("target", save);
		}

		if (!did_something) fizzle("Command doesn't have affector for current target");
		if ( out.size() == 0 ) return null;
		else if ( out.size() == 1 ) return out.iterator().next();
		else return Parameter.createList(out.toArray(new Parameter[0]));
		
	}
	
	protected static void fizzle() {
		throw new FizzleException();
	}
	
	protected static void fizzle(String why) {
		throw new FizzleException(why);
	}
	
	protected static void fizzleTarget(String string) {
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
		Parameter caster = ctx.getCaster();
		LivingEntity casterp = null;
		if ( caster != null ) casterp = caster.as(LivingEntity.class);
		
		List<Block> sight = null;
		int dist = 250;
		switch ( this.getDefaultTargetType(ctx, ctx.getSource()) ) {
			case None:
				break;
			case Self:
				t = ctx.getCaster();
				break;
			case World:
				World w = ctx.getWorld();
				if ( w == null ) w = ctx.getCaster().as(World.class);
				
				if ( w != null ) t = Parameter.from(w);
				break;
			case TargetBlock:
				if ( casterp == null ) return null;
				sight = casterp.getLastTwoTargetBlocks(null, dist);
				Block blk = null;
				if ( sight.size() < 2 ) {
					blk = casterp.getTargetBlock(null, dist);
					if ( blk.isEmpty() ) return null;
					return Parameter.from(blk);
				}
				blk = sight.get(1); 
				if( blk.isEmpty() ) return null;
				return Parameter.from(blk, blk.getFace(sight.get(0)));				
			case TargetPlace:
				if ( casterp == null ) return null;
				sight = casterp.getLastTwoTargetBlocks(null, dist);
				if ( sight.size() < 2 ) return null;
				Block blk2 = sight.get(0);
				return Parameter.from(blk2, blk2.getFace(sight.get(1)));
			case Server:
				return Parameter.from(Bukkit.getServer());
				
				
		}
		
		
		if ( t != null ) ctx.setTarget(t);
		return t;
	}
	
	protected boolean canAffect(Class<? extends Parameter> type) {
		return getAffectors().contains(type);
	}
	
	protected boolean getShouldCombindDuplicateListOutput() { return false; }

	
	protected <T extends Parameter> ParameterPtr tryAffect(Class<T> type, Parameter t, Context ctx) {
		String name;
		Class[] types;

		if ( type == null ) {
			name = "affectNull";
			types = new Class[] { Context.class };
			if ( !this.canAffect(null) ) return null;
		} else {
			if ( !type.isInstance(t) ) return null;
			if ( !this.canAffect(type) ) return null;
			name = "affect";
			types =  new Class[] { type, Context.class };
		}

		try {
			//Debug.trace.println("INVOKE " + t.getClass() + " bread inside " + this.getClass().getSimpleName());
			Method m = this.getClass().getMethod(name, types);
			Parameter p = type == null ? (Parameter) m.invoke(this, ctx) : (Parameter) m.invoke(this, t, ctx);
			ParameterPtr o = new ParameterPtr();
			o.setValue(p);
			return o;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if ( e.getTargetException() instanceof RuntimeException )
				throw (RuntimeException) e.getTargetException();
			if ( e.getTargetException() instanceof Error )
				throw (Error)e.getTargetException();
			
			throw new RuntimeException(e.getTargetException());
		}
		
	}
	
	public Context getSpellContext() {
		return this.spellStatic;
	}

	public String toString() {
		return "[" + this.getClass().getSimpleName() + " " + this.getName() + "]";
	}
}