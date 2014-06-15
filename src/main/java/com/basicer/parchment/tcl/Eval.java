package com.basicer.parchment.tcl;



import com.basicer.parchment.*;
import com.basicer.parchment.EvaluationResult.EvalCallback;
import com.basicer.parchment.parameters.Parameter;

public class Eval extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		if ( ctx.getArgs().size() == 0 ) return EvaluationResult.makeError("wrong # args: should be \"eval arg ?arg ...?\"");
		final Parameter expr = Concat.doConcat(ctx.getArgs());
		
		System.err.println(expr.asString());
		
		
		return new BranchEvaluationResult(expr.castToStringParameter(), ctx.up(1), new EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
				return last;
			}
		});
	}
}
