package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLParser;
import com.basicer.parchment.parameters.Parameter;

public class Proc extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "name", "argNames", "body" };
	}

	@Override
	public Parameter execute(Context ctx) {
		Parameter pname = ctx.get("name");
		Parameter pargs = ctx.get("argNames");
		Parameter pbody = ctx.get("body");
		
		final String[] xargs = pargs.asString().split(" ");
		final String bodystr = pbody.asString();
		
		TCLCommand proc = new TCLCommand() {

			@Override
			public String[] getArguments() { return xargs; }
			
			@Override
			public Parameter execute(Context ctx) {
				return TCLParser.evaluate(bodystr, ctx);
			}
			
		};
		ctx.up(1).setCommand(pname.asString(), proc);
		System.out.println("Registered proc " + pname.asString());
		return Parameter.from("");
		
	}

}
