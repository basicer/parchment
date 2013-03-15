package com.basicer.parchment.tcl;



import com.basicer.parchment.Debug;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.DelegateParameter;
import com.basicer.parchment.parameters.Parameter;


public class Global extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varname" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {

		Parameter t = ctx.getThis();
		Debug.trace("This = " + t);
		if ( t == null ) return EvaluationResult.makeError("Couldnt find spell context");
		DelegateParameter tt = t.cast(DelegateParameter.class);
		if ( tt == null ) return EvaluationResult.makeError("This wasent a delegate");
		TCLCommand cmd = tt.asTCLCommand(ctx);
		if ( !( cmd instanceof Spell )) return EvaluationResult.makeError("This wasent a Spell");

		Context src = ((Spell) cmd).getSpellContext();
		Debug.trace("This's spell " + ((Spell) cmd).getName() + " ctx has:" + src.getDebuggingString());
		ctx.up(1).linkVariableFromContext(src, ctx.get("varname").asString());
		
		return EvaluationResult.OK;
	}

}
