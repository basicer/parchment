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
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.parameters.Parameter;

public class Join extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "what" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String name = null;
		String what = ctx.get("what").asString();
		ParameterAccumulator[] tkns = TCLEngine.parseLine(new PushbackReader(new StringReader(what)), null, null);
		for ( ParameterAccumulator p : tkns ) {
			ctx.sendDebugMessage(") " + p.cheatyResolveOrFizzle().asString());
		}

		return EvaluationResult.OK;
		
		
	}

}
