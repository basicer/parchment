package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Unset extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "-nocomplain?", "args" }; }
	
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		Context ctxu = ctx.up(1);
		for ( Parameter p : ctx.getArgs() ) 
			Set.access(p.asString(), true,  null, ctxu);
		
		return EvaluationResult.OK;
	}

}
