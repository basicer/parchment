package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class Concat extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }

	
	@Override
	public Parameter execute(Context ctx) {
		StringBuilder out = null;
		Parameter args = ctx.get("args");
		for ( Parameter p : args ) {
			if ( out == null ) out = new StringBuilder();
			else out.append(" ");
			
			out.append(p.asString().trim());
		}
		return Parameter.from(out.toString());
	}

}
