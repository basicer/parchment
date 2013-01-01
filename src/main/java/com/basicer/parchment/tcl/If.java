package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLParser;
import com.basicer.parchment.parameters.Parameter;

public class If extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "expr", "'then'?", "body" }; }

	@Override
	public Parameter execute(Context ctx) { throw new RuntimeException("Wrong IF Invocation"); }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx) {
		Parameter expr = ctx.get("expr");
		Parameter ok = Expr.eval(expr.asString(), ctx.up(1));
		if ( ok == null ) throw new RuntimeException("Invalid expression: " + expr.asString());
		if ( ok.asBoolean() ) {
			return TCLParser.evaluate(ctx.get("body").asString(), ctx.up(1));
		}
		return new EvaluationResult(Parameter.from(""));
		
	}
}
