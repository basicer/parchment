package com.basicer.parchment.tclutil;



import com.basicer.parchment.*;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;

public class Repeat extends TCLCommand {

	private class IntHolder {
		int value = 0;
	}

	@Override
	public String[] getArguments() { return new String[] { "-targeting", "-i", "times", "body"  }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, final TCLEngine e) {
		final Parameter expr = ctx.get("test");
		final Context evalctx = ctx.up(1);
		final int times = ctx.get("times").asInteger(ctx);
		final String body =  ctx.get("body").asString();
		final IntHolder ctl = new IntHolder();
		final boolean target = ctx.has("targeting");
		final boolean seti = ctx.has("i");
		if ( seti ) {
			evalctx.put("i", IntegerParameter.from(1));
		}
		ctl.value = times;
		return new EvaluationResult.BranchEvaluationResult(body, evalctx, new EvaluationResult.EvalCallback(){
			public EvaluationResult result(EvaluationResult er) {
				if ( target ) {
					Parameter newtarget = er.getValue();
					evalctx.setTarget(newtarget);
					Debug.info("Target now: %s", newtarget == null ? "null" : newtarget.toString());
				}
				if ( seti ) evalctx.put("i", IntegerParameter.from(times - ctl.value + 2));
				if ( --ctl.value <= 0 ) return er;

				if ( er.getCode() == Code.ERROR ) return er;
				else if ( er.getCode() == Code.BREAK ) return new EvaluationResult(Parameter.EmptyString);
				else if ( er.getCode() == Code.RETURN ) return er;

				return new EvaluationResult.BranchEvaluationResult(body, evalctx, this);
			}
		});



	}
}
