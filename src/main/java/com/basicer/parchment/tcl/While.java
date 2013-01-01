package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLParser;
import com.basicer.parchment.parameters.Parameter;

public class While extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "test", "body" }; }

	@Override
	public Parameter execute(Context ctx) { throw new RuntimeException("Wrong while Invocation"); }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx) {
		Parameter expr = ctx.get("test");
		
		EvaluationResult result = new EvaluationResult(Parameter.from(""));
		int rounds = 0;
		while ( true ) {
			if ( ++rounds > 20 ) break;
			Parameter ok = Expr.eval(expr.asString(), ctx.up(1));
			if ( ok == null ) throw new RuntimeException("Invalid expression: " + expr.asString());
			if ( ok.asBoolean() ) {
				result = TCLParser.evaluate(ctx.get("body").asString(), ctx.up(1));
			} else {
				break;
			}
			
		}
		return result;
		
	}
}
