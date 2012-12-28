package com.basicer.parchment;

import java.io.PushbackReader;
import java.util.HashMap;

import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.Parameter;

public class ScriptedSpell extends Spell {
	
	private HashMap<String, String> triggers;
	private HashMap<Class<? extends Parameter>, String> affects;
	
	@Override
	public DefaultTargetType getDefaultTargetType(Context ctx) {
		Parameter p = this.spellStatic.get("defaultTargetType");
		if ( p == null ) return super.getDefaultTargetType(ctx);
		String s = p.asString();
		if ( s == null ) return DefaultTargetType.None;
		System.out.println("I see you want: " + s);
		if ( s.equals("self") ) return DefaultTargetType.Self;
		
		return DefaultTargetType.None;
	}


	public ScriptedSpell(String source, SpellFactory f) {
		super();
		triggers = new HashMap<String, String>();
		affects = new HashMap<Class<? extends Parameter>, String>();
		spellStatic.put("this", Parameter.from(this));
		spellStatic.setSpellFactory(f);
		TCLParser.evaluate(source, this.spellStatic);
		spellStatic.setSpellFactory(null);
	}


	public ScriptedSpell(PushbackReader source, SpellFactory f) {
		super();
		triggers = new HashMap<String, String>();
		affects = new HashMap<Class<? extends Parameter>, String>();
		spellStatic.put("this", Parameter.from(this));
		spellStatic.setSpellFactory(f);
		TCLParser.evaluate(source, this.spellStatic);
		spellStatic.setSpellFactory(null);
	}

	public void setTrigger(String name, String source) {
		triggers.put(name, source);
	}
	
	public void setAffect(Class<? extends Parameter> name, String source) {
		affects.put(name, source);
	}
	
	@Override
	public Parameter cast(final Context ctx) {
		String script = triggers.get("cast");
		final Spell closure_s = this;
		
		DefaultTargetType tt = getDefaultTargetType(ctx);
		if ( tt != DefaultTargetType.None ) {
			ctx.put("target", resolveTarget(ctx));
		}
		
		ctx.put("super", Parameter.from(new TCLCommand() {
			@Override
			public Parameter execute(Context ctx) {
				// TODO Auto-generated method stub
				return null;
			}
		}));
		if ( script != null ) {
			ctx.sendDebugMessage("EVAL:" + script);
			Parameter r = TCLParser.evaluate(script, ctx);
		} else {
			super.cast(ctx);
		}
		
		return null;
	}

	

	public Parameter affect(Parameter target, Context ctx) {
		ctx.sendDebugMessage("And so it was");
		return null;
	}

	
}
