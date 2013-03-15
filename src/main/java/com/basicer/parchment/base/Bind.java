package com.basicer.parchment.base;

import com.basicer.parchment.Debug;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.DelegateParameter;
import com.basicer.parchment.parameters.Parameter;


public class Bind extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "trigger", "procedure" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		return new EvaluationResult(execute(ctx));
	}
		
	public Parameter execute(Context ctx) {
		Parameter t = ctx.getThis();
		Debug.trace("This = " + t);
		if ( t == null ) return Parameter.from(false);
		DelegateParameter tt = t.cast(DelegateParameter.class);
		if ( tt == null ) return Parameter.from(false);
		TCLCommand cmd = tt.as(TCLCommand.class);
		if ( cmd == null ) return Parameter.from(false);
		if ( !( cmd instanceof ScriptedSpell )) return Parameter.from(false);

		ScriptedSpell ss = (ScriptedSpell) cmd;
		ss.setTrigger(ctx.get("trigger").asString(), ctx.get("procedure").asString());
		Debug.trace("Bound " + ctx.get("trigger").asString()  + " as " + ctx.get("procedure").asString());
		return Parameter.from(true);
		
	}

}
