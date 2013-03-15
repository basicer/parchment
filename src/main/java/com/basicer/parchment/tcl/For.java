package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class For extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "start", "test", "next", "body"  }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter expr = ctx.get("test");
		Context evalctx = ctx.up(1);
		e.evaluate(ctx.get("start").asString(), evalctx);
		
		while ( true ) {
		Parameter ok = Expr.eval(expr.asString(), evalctx, e);
		if ( ok == null ) throw new RuntimeException("Invalid expression: " + expr.asString());
		if ( !ok.asBoolean() ) break;
			EvaluationResult er = e.evaluate(ctx.get("body").asString(), evalctx);
			if ( er.getCode() == Code.BREAK ) break;
			if ( er.getCode() == Code.RETURN ) return er;
			e.evaluate(ctx.get("next").asString(), evalctx);
		}
		
		return new EvaluationResult(Parameter.EmptyString);
		
	}
}
