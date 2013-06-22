package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

public class LAppend extends TCLCommand {

	
	
	@Override
	public String[] getArguments() {
		return new String[] { "varName", "args" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		int argc = ctx.getArgs().size();
		String varname = ctx.get("varName").asString(ctx);
		Parameter p = Set.access(varname, false, null, ctx.up(1));
		if ( p == null ) return EvaluationResult.makeError("Couldnt find variable: " + varname); 
		ListParameter list = p.cast(ListParameter.class);
		if ( list == null ) return EvaluationResult.makeError("Not a list?!");
		for ( int i = 0; i < argc; ++i ) {
			list.add(ctx.getArgs().get(i));
			
		}
		
		return new EvaluationResult(list);
	}

}
