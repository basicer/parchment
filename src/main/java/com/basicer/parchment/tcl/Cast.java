package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Cast extends TCLCommand {

	@Override
	public Parameter execute(Context ctx) { 
		Parameter what = ctx.get("what");
		String type = ctx.get("type").asString();
		
		Parameter out = what.castByString(type, ctx);
		if ( out == null ) Parameter.from("null");
		return out;
		
		
	}

	@Override
	public String[] getArguments() { return new String[] { "what", "type" }; }

	
	

}