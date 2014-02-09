package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

import java.util.ArrayList;

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
		if ( list == null ) {
			ArrayList<Parameter> x = new ArrayList<Parameter>();
			x.add(p);
			list = ListParameter.from(x);
		}
		for ( int i = 0; i < argc; ++i ) {
			list.add(ctx.getArgs().get(i));
			
		}

		Set.access(varname, true, list, ctx.up(1));
		return EvaluationResult.makeOkay(list);
	}

}
