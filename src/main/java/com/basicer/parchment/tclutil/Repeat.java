package com.basicer.parchment.tclutil;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class Repeat extends TCLCommand {

	private class IntHolder {
		int value = 0;
	}

	@Override
	public String[] getArguments() { return new String[] { "times", "body"  }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, final TCLEngine e) {
		final Parameter expr = ctx.get("test");
		final Context evalctx = ctx.up(1);
		final int times = ctx.get("times").asInteger(ctx);
		final String body =  ctx.get("body").asString();
		final IntHolder ctl = new IntHolder();
		ctl.value = times;
		return new EvaluationResult.BranchEvaluationResult(body, evalctx, new EvaluationResult.EvalCallback(){
			public EvaluationResult result(EvaluationResult er) {
				if ( --ctl.value <= 0 ) return er;
				if ( er.getCode() == Code.BREAK ) return new EvaluationResult(Parameter.EmptyString);
				if ( er.getCode() == Code.RETURN ) return er;

				return new EvaluationResult.BranchEvaluationResult(body, evalctx, this);
			}
		});



	}
}
