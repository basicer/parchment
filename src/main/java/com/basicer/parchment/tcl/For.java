package com.basicer.parchment.tcl;



import com.basicer.parchment.*;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class For extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "start", "test", "next", "command"  }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, final TCLEngine e) {
		final Parameter expr = ctx.get("test");
		final Context evalctx = ctx.up(1);
		
		
		return new BranchEvaluationResult(ctx.get("start").castToStringParameter(), evalctx, new EvaluationResult.EvalCallback() {
			
			public EvaluationResult result(EvaluationResult last) {
				final EvaluationResult.EvalCallback again = this;

				//Allow break in the 3rd argument.
				if ( last != null && last.getCode() == Code.BREAK ) return EvaluationResult.makeOkay(Parameter.EmptyString);

				Parameter ok = Expr.eval(expr.asString(), evalctx, e);
				if ( ok == null ) throw new RuntimeException("Invalid expression: " + expr.asString());
				if ( !ok.asBoolean() ) return EvaluationResult.makeOkay(Parameter.EmptyString);
				return new BranchEvaluationResult(ctx.get("command").castToStringParameter(), evalctx, new EvaluationResult.EvalCallback(){
					public EvaluationResult result(EvaluationResult er) {
						if ( er.getCode() == Code.BREAK ) return EvaluationResult.makeOkay(Parameter.EmptyString);
						if ( er.getCode() == Code.RETURN ) return er;
						return new BranchEvaluationResult(ctx.get("next").castToStringParameter(), evalctx, again);
					}
				});

			}
		});
				
				
		
	}
}
