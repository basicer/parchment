package com.basicer.parchment;

import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.basicer.parchment.Context.ParameterPtr;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.Parameter;

public class ScriptedSpell extends Spell {
	
	private HashMap<String, String> triggers;
	
	@Override
	public DefaultTargetType getDefaultTargetType(Context ctx, String source) {
		Parameter p = this.spellStatic.get("defaultTargetType");
		if ( p == null ) return super.getDefaultTargetType(ctx, source);
		String s = p.asString();
		if ( s == null ) return DefaultTargetType.None;
		System.out.println("I see you want: " + s);
		if ( s.equals("self") ) return DefaultTargetType.Self;
		if ( s.equals("block") ) return DefaultTargetType.TargetBlock;
		if ( s.equals("place") ) return DefaultTargetType.TargetPlace;
		return DefaultTargetType.None;
	}


	public ScriptedSpell(String source, SpellFactory f) {
		super();
		triggers = new HashMap<String, String>();
		spellStatic.put("this", Parameter.from(this));
		spellStatic.setSpellFactory(f);
		TCLParser.evaluate(source, this.spellStatic);
		spellStatic.setSpellFactory(null);
	}


	public ScriptedSpell(PushbackReader source, SpellFactory f) {
		super();
		triggers = new HashMap<String, String>();
		spellStatic.put("this", Parameter.from(this));
		spellStatic.setSpellFactory(f);
		TCLParser.evaluate(source, this.spellStatic);
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
		// TODO Auto-generated method stub
		return super.tryAffect(type, t, ctx);
	}


	@Override
	protected List<Class<? extends Parameter>> getAffectors() {
		List<Class<? extends Parameter>> out = new ArrayList<Class<? extends Parameter>>();
		for ( String s : triggers.keySet() ) {
			if ( s.startsWith("affect:") ) continue;
			
			
		}
		
		return out;
	}

	
	@Override
	public Parameter cast(final Context ctx) {
		String name = triggers.get("cast");
		final Spell closure_s = this;
		
		DefaultTargetType tt = getDefaultTargetType(ctx,ctx.getSource());

		Parameter target = null;
		if ( tt != DefaultTargetType.None ) {
			target = resolveTarget(ctx);
		}

		ctx.put("super", Parameter.from(new TCLCommand() {
			@Override
			public Parameter execute(Context ctx) {
				return Spell.defaultCastBehavior(closure_s, ctx);
			}
		}));
		if ( name != null ) {
			System.out.println("-> DELEGATE TO " + name);
			TCLCommand proc = ctx.getCommand(name);
			ArrayList<Parameter> argz = ctx.getArgs();
			Parameter[] up = new Parameter[argz.size() + 1];
			up[0] = Parameter.from(name);
			for ( int i = 0; i < argz.size(); ++i) up[i+1] = argz.get(i);
				
			
			Context ctx2 = proc.bindContext(up, ctx.up(1));
			if ( target != null ) ctx2.setTarget(target);
			return proc.execute(ctx2);
			
			
		} else {
			return super.cast(ctx);
		}
		
	}

	

	public Parameter affect(Parameter target, Context ctx) {
		ctx.sendDebugMessage("And so it was");
		return null;
	}

	
}
