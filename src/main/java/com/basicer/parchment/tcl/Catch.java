package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class Catch extends TCLCommand {
	@Override
	public String[] getArguments() { return new String[] { "script", "messageVarName?"  }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter code = ctx.get("script");
		EvaluationResult ir = e.evaluate(code.asString(), ctx);
		if ( ctx.get("messageVarName") != null ) {
			Set.access( ctx.get("messageVarName").asString(), true, ir.getValue(), ctx.up(1));
		}
		
		return new EvaluationResult(Parameter.from(ir.getCode().ordinal()));
	}
}
