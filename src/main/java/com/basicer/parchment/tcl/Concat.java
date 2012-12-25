package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class Concat extends TCLCommand {


	@Override
	public Parameter execute(Context ctx) {
		StringBuilder out = null;
		for ( Parameter p : ctx.getArgs() ) {
			if ( out == null ) out = new StringBuilder();
			else out.append(" ");
			
			out.append(p.asString().trim());
		}
		if ( out == null ) return Parameter.from("");
		return Parameter.from(out.toString());
	}

}
