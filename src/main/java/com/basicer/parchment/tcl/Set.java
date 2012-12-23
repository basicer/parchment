package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class Set extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "value" }; }
	
	@Override
	public Parameter execute(Context ctx) {
		String name = ctx.get("varName").asString();
		Parameter val = ctx.get("value");
		Context ctxu = ctx.up(1);
		if ( val != null ) {
			ctxu.put(name, val);
		}
		
		return ctxu.get(name);	
	}

}
