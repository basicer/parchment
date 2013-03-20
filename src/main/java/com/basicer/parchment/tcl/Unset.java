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

public class Unset extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "-nocomplain?", "args" }; }
	
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		Context ctxu = ctx.up(1);
		for ( Parameter p : ctx.getArgs() ) 
			Set.access(p.asString(), true,  null, ctxu);
		
		return EvaluationResult.OK;
	}

}