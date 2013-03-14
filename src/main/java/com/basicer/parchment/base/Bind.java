package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.SpellParameter;

public class Bind extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "trigger", "procedure" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		return new EvaluationResult(execute(ctx));
	}
		
	public Parameter execute(Context ctx) {
		Parameter t = ctx.resolve("this");
		System.out.println("This = " + t);
		if ( t == null ) return Parameter.from(false);
		SpellParameter tt = t.cast(SpellParameter.class);
		if ( tt == null ) return Parameter.from(false);
		
		Spell s = tt.as(Spell.class);
		if ( s == null ) return Parameter.from(false);
		if ( !( s instanceof ScriptedSpell )) return Parameter.from(false);

		ScriptedSpell ss = (ScriptedSpell) s;
		ss.setTrigger(ctx.get("trigger").asString(), ctx.get("procedure").asString());
		System.out.println("Bound " + ctx.get("trigger").asString()  + " as " + ctx.get("procedure").asString());
		return Parameter.from(true);
		
	}

}
