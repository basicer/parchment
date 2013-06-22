package com.basicer.parchment.tclutil;



import com.basicer.parchment.Debug;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.DelegateParameter;
import com.basicer.parchment.parameters.Parameter;


public class Static extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {

		Parameter t = ctx.getThis();
		Debug.trace("This = " + t);
		if ( t == null ) return EvaluationResult.makeError("Couldn't find spell context");
		DelegateParameter tt = t.cast(DelegateParameter.class);
		if ( tt == null ) return EvaluationResult.makeError("This wasen't a delegate");
		TCLCommand cmd = tt.asTCLCommand(ctx);
		if ( !( cmd instanceof Spell )) return EvaluationResult.makeError("This wasent a Spell");

		Context src = ((Spell) cmd).getSpellContext();
		Debug.trace("This spell " + ((Spell) cmd).getName() + " ctx has:" + src.getDebuggingString());
		for ( Parameter p : ctx.getArgs() ) {
			ctx.up(1).linkVariableFromContext(src, p.asString());
		}
		
		return EvaluationResult.OK;
	}

}
