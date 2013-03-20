package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

public class LIndex extends TCLCommand {

	
	
	@Override
	public String[] getArguments() {
		return new String[] { "list", "args" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		int argc = ctx.getArgs().size();
		Parameter list = ctx.get("list").cast(ListParameter.class);
		for ( int i = 0; i < argc; ++i ) {
			list = list.cast(ListParameter.class);
			int index = ctx.getArgs().get(i).asInteger();
			list = list.index(index);
		}
		
		return new EvaluationResult(list);
	}

}
