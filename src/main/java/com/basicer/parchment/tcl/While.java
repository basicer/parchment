package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.EvaluationResult.EvalCallback;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class While extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "test", "command" };
	}

	@Override
	public EvaluationResult extendedExecute(final Context ctx, final TCLEngine engine) {
		final Parameter expr = ctx.get("test");
		final Context evalctx = ctx.up(1);
		EvaluationResult result = new EvaluationResult(Parameter.from(""));
		int rounds = 0;

		return new EvaluationResult.BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {
			int	rounds	= 0;

			public EvaluationResult result(EvaluationResult er) {
				final EvalCallback again = this;
				if ( er instanceof EvaluationResult.BranchEvaluationResult ) {
					//First time though
				} else {
					if (er.getCode() == Code.BREAK) return EvaluationResult.OK;
				}
				Parameter ok = Expr.eval(expr.asString(), evalctx, engine);
				if (ok == null)
					throw new RuntimeException("Invalid expression: " + expr.asString());
				if (ok.asBooleanStrict(ctx)) {
					// result = engine.evaluate(ctx.get("body").asString(),
					// evalctx);
					return new EvaluationResult.BranchEvaluationResult(ctx.get("command").asString(), evalctx, again);
				} else {
					return EvaluationResult.OK;
				}

			}
		});

	}
}
