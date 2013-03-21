package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.EvalCallback;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class Catch extends TCLCommand {
	@Override
	public String[] getArguments() { return new String[] { "script", "messageVarName?"  }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, TCLEngine e) {
		final Parameter code = ctx.get("script");

		
		return new EvaluationResult.BranchEvaluationResult(code.asString(), ctx.up(1), new EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
				if ( ctx.get("messageVarName") != null ) {
					Set.access( ctx.get("messageVarName").asString(), true, last.getValue(), ctx.up(1));
				}
				return new EvaluationResult(Parameter.from(last.getCode().ordinal()));
			}
			
			
		});
		
		//
	}
}
