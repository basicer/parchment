package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class Upvar extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "level", "args" }; }
	
	@Override
	public Parameter execute(Context ctx) {
		int level = ctx.get("level").asInteger();
		
		Context tctx = ctx.up(1);
		
		for ( Parameter p : ctx.get("args") ) {
			tctx.upvar(level, p.asString());
		}
		
		
		
		return null;
	}

}
