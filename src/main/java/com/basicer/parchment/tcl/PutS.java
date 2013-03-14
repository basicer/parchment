package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class PutS extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "string" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter s = ctx.get("string");
		if ( s == null ) {
			ctx.sendDebugMessage("null");
			return EvaluationResult.OK;
		}
		ctx.sendDebugMessage(s.asString());
		return new EvaluationResult(s);
	}
	
	
	
}
