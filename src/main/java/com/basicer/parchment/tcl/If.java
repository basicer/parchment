package com.basicer.parchment.tcl;



import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLParser;
import com.basicer.parchment.parameters.Parameter;

public class If extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "expr", "'then'?", "body" }; }

	@Override
	public Parameter execute(Context ctx) {
		Parameter expr = ctx.get("expr");
		Parameter ok = Expr.eval(expr.asString(), ctx.up(1));
		ctx.sendDebugMessage(ok.toString());
		if ( ok.asBoolean() ) {
			ctx.sendDebugMessage("Eval: " + ctx.get("body").asString());
			return TCLParser.evaluate(ctx.get("body").asString(), ctx.up(1));
		}
		return Parameter.from("");
	}
	
	

}
