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
		Parameter list = ctx.get("list");
		for ( int i = 0; i < argc; ++i ) {
			Parameter list2 = list.cast(ListParameter.class);
			if ( list2 != null ) list = list2;
			int index = ctx.getArgs().get(i).asInteger();
			try {
				list = list.index(index);
			} catch ( IndexOutOfBoundsException ex ) {
				return new EvaluationResult(Parameter.from(""), EvaluationResult.Code.OK);
			}
		}
		
		return new EvaluationResult(list);
	}

}
