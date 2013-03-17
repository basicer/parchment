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

		Context src = ctx;
		
		while ( src.up(1) != null ) src = src.up(1);
		ctx.up(1).linkVariableFromContext(src, ctx.get("varname").asString());
		
		return EvaluationResult.OK;
	}

}
