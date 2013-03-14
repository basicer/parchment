package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Concat extends TCLCommand {


	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		StringBuilder out = null;
		for ( Parameter p : ctx.getArgs() ) {
			if ( out == null ) out = new StringBuilder();
			else out.append(" ");
			
			out.append(p.asString().trim());
		}
		if ( out == null ) return EvaluationResult.OK;
		return new EvaluationResult(Parameter.from(out.toString()));
	}

}
