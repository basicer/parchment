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
	public String[] getArguments() { return new String[] { "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {

		Context src = ctx.top();
		Context ctxu = ctx.up(1);
		
		Debug.trace("Reday For %d", ctx.getArgs().size());
				
		for ( Parameter p : ctx.getArgs() ) {
			Debug.trace("Linking %s", p.asString());
			ctxu.linkVariableFromContext(src, p.asString());
		}
		
		Debug.trace("Go %s",ctxu.getDebuggingString());
		
		
		return EvaluationResult.OK;
	}

}
