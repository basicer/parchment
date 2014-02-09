package com.basicer.parchment.tcl;



import com.basicer.parchment.*;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class For extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "start", "test", "next", "command"  }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, final TCLEngine e) {
		final Parameter expr = ctx.get("test");
		final Context evalctx = ctx.up(1);
		
		
		return new BranchEvaluationResult(ctx.get("start").asString(), evalctx, new EvaluationResult.EvalCallback() {
			
			public EvaluationResult result(EvaluationResult last) {
				final EvaluationResult.EvalCallback again = this;
				Parameter ok = Expr.eval(expr.asString(), evalctx, e);
				if ( ok == null ) throw new RuntimeException("Invalid expression: " + expr.asString());
				if ( !ok.asBoolean() ) return new EvaluationResult(Parameter.EmptyString);
				return new BranchEvaluationResult(ctx.get("command").asString(), evalctx, new EvaluationResult.EvalCallback(){
					public EvaluationResult result(EvaluationResult er) {
						if ( er.getCode() == Code.BREAK ) return new EvaluationResult(Parameter.EmptyString);
						if ( er.getCode() == Code.RETURN ) return er;
						return new BranchEvaluationResult(ctx.get("next").asString(), evalctx, again);
					}
				});

			}
		});
				
				
		
	}
}
