package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;

public class If extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "expr", "'then'?", "body", "'else'?", "elseCode?" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter expr = ctx.get("expr");
		final Context evalctx = ctx.up(1);
		Parameter ok = Expr.eval(expr.asString(), evalctx, e);
		if (ok == null)
			throw new RuntimeException("Invalid expression: " + expr.asString());
		if (ok.asBoolean()) {
			return new BranchEvaluationResult(ctx.get("body").castToStringParameter(), evalctx,
					new EvaluationResult.EvalCallback() {
						public EvaluationResult result(EvaluationResult er) {
							return er; // No processing of result needed
						}
					});
		} else if (ctx.get("else") != null) {
			String code = ctx.get("elseCode").asString();
			if (code.length() > 0) {
				return new BranchEvaluationResult(ctx.get("elseCode").castToStringParameter(), evalctx,
						new EvaluationResult.EvalCallback() {
							public EvaluationResult result(EvaluationResult er) {
								return er; // No processing of result needed
							}
						});
			} else
				return EvaluationResult.OK;
		} else if (ctx.get("elseCode") != null) {
			return EvaluationResult.makeError("Use else noise word in an if/else statement");
		} else {
			return EvaluationResult.makeOkay(Parameter.from(""));
		}

	}
}
