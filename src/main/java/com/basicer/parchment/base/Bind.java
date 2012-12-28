package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.SpellParameter;

public class Bind extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "trigger", "procedure" }; }
	
	@Override
	public Parameter execute(Context ctx) {
		Parameter t = ctx.resolve("this");
		if ( t == null ) return Parameter.from(false);
		SpellParameter tt = t.cast(SpellParameter.class);
		if ( tt == null ) return Parameter.from(false);
		
		Spell s = tt.asSpell();
		if ( s == null ) return Parameter.from(false);
		if ( !( s instanceof ScriptedSpell )) return Parameter.from(false);

		ScriptedSpell ss = (ScriptedSpell) s;
		ss.setTrigger(ctx.get("trigger").asString(), ctx.get("procedure").asString());
		
		return Parameter.from(true);
		
	}

}
