package com.basicer.parchment.tcl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;

public class Incr extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "increment?" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String varName = ctx.get("varName").asString();
		Parameter incr = ctx.get("increment");
		int ammount = 1;
		
		if ( incr != null && incr.asInteger() != null ) ammount = incr.asInteger();
		
		Context ctxu = ctx.up(1);
		Parameter current = Set.access(varName, false, null, ctxu);
		if ( current.asInteger() == null ) return EvaluationResult.makeError("Can only increment integers"); 
		return new EvaluationResult(Set.access(varName, true, Parameter.from(current.asInteger() + ammount), ctxu));
		
		
	}

}
