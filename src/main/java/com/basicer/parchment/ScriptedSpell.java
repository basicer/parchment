package com.basicer.parchment;

import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.basicer.parchment.Context.ParameterPtr;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;

public class ScriptedSpell extends Spell {
	
	private HashMap<String, String> triggers;
	private String name;
	
	@Override
	public String getName() { return name; }
	
	@Override
	public DefaultTargetType getDefaultTargetType(Context ctx, String source) {
		Parameter p = this.spellStatic.get("defaultTargetType");
		if ( p == null ) return super.getDefaultTargetType(ctx, source);
		String s = p.asString();
		if ( s == null ) return DefaultTargetType.None;
		Debug.trace("I see you want: " + s);
		if ( s.equals("self") ) return DefaultTargetType.Self;
		if ( s.equals("block") ) return DefaultTargetType.TargetBlock;
		if ( s.equals("place") ) return DefaultTargetType.TargetPlace;
		return DefaultTargetType.None;
	}


	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		//We need to throw out scripted spell's parameter context.
		Debug.trace("EE with " + ctx.getDebuggingString());
		return executeBinding("cast", ctx.up(1), e, ctx.getArgs());
	}
	
	
	public ScriptedSpell(String name, String source, SpellFactory f) {
		super();
		this.name = name;
		triggers = new HashMap<String, String>();
		spellStatic.put("this", Parameter.from(this));
		spellStatic.setSpellFactory(f);
		TCLUtils.evaluate(source, this.spellStatic);
		spellStatic.setSpellFactory(null);
	}


	public ScriptedSpell(String name, PushbackReader source, SpellFactory f) {
		super();
		this.name = name;
		triggers = new HashMap<String, String>();
		spellStatic.put("this", Parameter.from(this));
		spellStatic.setSpellFactory(f);
		TCLUtils.evaluate(source, this.spellStatic);
		spellStatic.setSpellFactory(null);
	}

	public void setTrigger(String name, String source) {
		triggers.put(name, source);
	}
	
	public void setAffect(Class<? extends Parameter> name, String source) {
		triggers.put("affect:" + name.getSimpleName(), source);
	}
	
	
	
	@Override
	protected <T extends Parameter> ParameterPtr tryAffect(Class<T> type, Parameter t, Context ctx) {
		if ( !type.isInstance(t) ) return null;
		if ( !this.canAffect(type) ) return null;
		Debug.trace("T IS " + t);
		if ( type != PlayerParameter.class ) return null;
		executeBinding("affect:player", ctx, null);
		ParameterPtr x = new ParameterPtr();
		x.val = Parameter.EmptyString;
		return x;
	}


	@Override
	protected List<Class<? extends Parameter>> getAffectors() {
		List<Class<? extends Parameter>> out = new ArrayList<Class<? extends Parameter>>();
		for ( String s : triggers.keySet() ) {
			if ( !s.startsWith("affect:") ) continue;
			if ( s.equals("affect:player") ) {
				out.add(PlayerParameter.class);
			}
			
		}
		
		return out;
	}


	
	@Override
	public Parameter cast(Context ctx) {
		return executeBinding("cast", ctx, null, ctx.getArgs()).getValue(); //TODO: These would like an engine.
	}
	
	
	@Override
	public EvaluationResult executeBinding(String binding, final Context ctx, final TCLEngine engine) {
		return executeBinding(binding, ctx, engine, new ArrayList<Parameter>());
	}
	
	public EvaluationResult executeBinding(String binding, final Context ctx, final TCLEngine engine,  ArrayList<Parameter> argz) {
		
		String name = triggers.get(binding);
		Debug.trace("LeCasting : " + name + " for " + binding);
		final Spell closure_s = this;
		
		DefaultTargetType tt = getDefaultTargetType(ctx,ctx.getSource());

		Parameter target = null;
		if ( tt != DefaultTargetType.None ) {
			target = resolveTarget(ctx);
		}


		if ( name != null ) {
			Debug.trace("-> DELEGATE TO " + name);
			TCLCommand proc = spellStatic.getCommand(name);
			Parameter[] up = new Parameter[argz.size() + 1];
			up[0] = Parameter.from(name);
			for ( int i = 0; i < argz.size(); ++i) up[i+1] = argz.get(i);
				
			
			Context cmp = ctx.copyAndMergeProcs(this.spellStatic);
			final  Context ctx2 = proc.bindContext(up, cmp);


			/* if ( binding.equals("affect") ) {
				ctx2.putProc("super", new TCLCommand() {
					@Override
					public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
						return new EvaluationResult(Spell.applyAffectors(closure_s, ctx));
					}
				});
			} else */ if ( binding.equals("cast") ) {
				ctx2.putProc("super", new TCLCommand() {
					@Override
					public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
						return new EvaluationResult(closure_s.applyAffectors(closure_s, ctx2));
					}
				});
			}
			
			if ( target != null ) ctx2.setTarget(target);
			/*
			Debug.trace("Ending bindsing with " + ctx.getDebuggingString());
			Debug.trace("--------------------");
			Debug.trace("CMP " + cmp.getDebuggingString());
			Debug.trace("--------------------");
			*/
			Debug.trace("Brokering SC proc " + ctx2.getDebuggingString());
			
			return proc.extendedExecute(ctx2, engine);
			
			
		} else {
			return new EvaluationResult(super.cast(ctx));
		}
		
	}

	

	public Parameter affect(Parameter target, Context ctx) {
		return executeBinding("affect", ctx, null).getValue(); //TODO: These would like an engine.
	}

	
}
