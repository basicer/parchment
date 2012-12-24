package com.basicer.parchment;

import java.io.PushbackReader;

import com.basicer.parchment.parameters.Parameter;

public class ScriptedSpell extends Spell implements Affectable<Parameter> {
	
	public ScriptedSpell(String source) {
		super();
		System.out.println(source);
		spellStatic.put("this", Parameter.from(this));
		TCLParser.evaluate(source, this.spellStatic);
	}

	public ScriptedSpell(PushbackReader source) {
		super();
		spellStatic.put("this", Parameter.from(this));
		TCLParser.evaluate(source, this.spellStatic);
	}
	
	
	@Override
	public void cast(Context ctx) {
		Parameter script = ctx.up(1).get("cast");
		if ( script != null ) {
			ctx.sendDebugMessage("EVAL:" + script.asString());
			Parameter r = TCLParser.evaluate(script.asString(), ctx.createSubContext());
			if ( false ) super.cast(ctx);	
		} else {
			super.cast(ctx);
		}
		
	}



	public void affect(Parameter target, Context ctx) {
		ctx.sendDebugMessage("And so it was");
		
	}

	
}
