package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Type extends TCLCommand {

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter what = ctx.get("what");
		Parameter type = ctx.get("type");
		
		
		if ( type != null ) {
			Parameter out = what.castByString(type.asString(), ctx);
			if ( out == null ) return EvaluationResult.makeOkay(Parameter.from(""));
			return EvaluationResult.makeOkay(out);
		}
		
		return EvaluationResult.makeOkay(Parameter.from(what.getClass().getSimpleName()));
		
		
	}

	@Override
	public String[] getArguments() { return new String[] { "what", "type?" }; }

	
	

}