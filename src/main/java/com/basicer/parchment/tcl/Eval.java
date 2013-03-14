package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class Eval extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "code" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter expr = ctx.get("code");
		System.err.println(expr.asString());
		return e.evaluate(expr.asString(), ctx.up(1));		
	}
}
