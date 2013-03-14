package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class If extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "expr", "'then'?", "body", "'else'?", "elseCode?"  }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter expr = ctx.get("expr");
		
		Parameter ok = Expr.eval(expr.asString(), ctx.up(1), e);
		if ( ok == null ) throw new RuntimeException("Invalid expression: " + expr.asString());
		if ( ok.asBoolean() ) {
			return e.evaluate(ctx.get("body").asString(), ctx.up(1));
		} else if ( ctx.get("else") != null ) {
			String code = ctx.get("elseCode").asString();
			if ( code.length() > 0 ) return e.evaluate(code, ctx.up(1));
			else return new EvaluationResult(Parameter.from(""));
		} else if ( ctx.get("elseCode") != null ) {
			return EvaluationResult.makeError("Use else noise word in an if/else statement");
		} else {
			return new EvaluationResult(Parameter.from(""));
		}
		
		
	}
}
