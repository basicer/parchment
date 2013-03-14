package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class Uplevel extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "level", "code" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter level = ctx.get("level");
		Parameter expr = ctx.get("code");
		
		Context ectx = ctx.up(level.asInteger());
		if ( ectx == null ) return EvaluationResult.makeError("Invalid level given to uplevel");
		return e.evaluate(expr.asString(), ectx);		
	}
}
