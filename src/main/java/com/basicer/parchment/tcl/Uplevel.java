package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;

public class Uplevel extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "level", "code" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter level = ctx.get("level");
		Parameter expr = ctx.get("code");

		Context ectx = ctx.up(level.asInteger());
		if (ectx == null)
			return EvaluationResult.makeError("Invalid level given to uplevel");
		return new BranchEvaluationResult(expr.castToStringParameter(), ectx, new EvaluationResult.EvalCallback() {
			public EvaluationResult result(EvaluationResult er) {
				return er; // No processing of result needed
			}
		});
	}
}
