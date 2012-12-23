package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class PutS extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "string" }; }

	@Override
	public Parameter execute(Context ctx) {
		Parameter s = ctx.get("string");
		if ( s == null ) {
			ctx.sendDebugMessage("null");
			return null;
		}
		ctx.sendDebugMessage(s.asString());
		return s;
	}
	
	
	
}
