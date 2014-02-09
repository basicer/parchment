package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Incr extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "increment?" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String varName = ctx.get("varName").asString();
		Parameter incr = ctx.get("increment");
		long amount = 1;
		
		if ( incr != null ) {
			if ( incr.asLong() == null ) return EvaluationResult.makeError("expected integer but got \"" + incr.asString() + "\"");
			amount = incr.asLong();
		}
		
		Context ctxu = ctx.up(1);
		Parameter current;
		try {
			current = Set.access(varName, false, null, ctxu);
		} catch ( FizzleException ex ) { //TODO: Is there a cleaner way?
			//Incr on an empty variable starts it at one.
			return EvaluationResult.makeOkay(Set.access(varName, true, Parameter.from(1), ctxu));
		}
		if ( current.asInteger() == null ) return EvaluationResult.makeError("expected integer but got \"" + current.asString() + "\""); 
		return EvaluationResult.makeOkay(Set.access(varName, true, Parameter.from(current.asLong() + amount), ctxu));
		
		
	}

}
