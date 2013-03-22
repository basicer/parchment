package com.basicer.parchment.tcl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;

public class Append extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String varName = ctx.get("varName").asString();
		
		
		Context ctxu = ctx.up(1);
		String current;
		try {
			current = Set.access(varName, false, null, ctxu).asString();
		} catch ( FizzleException ex ) { //TODO: Is there a cleaner way?
			current = "";
		}
		
		for ( Parameter p : ctx.getArgs() ) {
			current += p.asString();
		}
		
		return new EvaluationResult(Set.access(varName, true, Parameter.from(current), ctxu));
		
		
	}

}
