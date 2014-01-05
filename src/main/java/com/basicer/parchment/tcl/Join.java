package com.basicer.parchment.tcl;

import java.io.PushbackReader;
import java.io.StringReader;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.parameters.Parameter;

public class Join extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "list", "joinstring?" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String name = null;
		String what = ctx.get("list").asString();
		String sep = " ";
		if ( ctx.has("joinstring") ) {
			Parameter p = ctx.get("joinstring");
			if ( p != null ) sep = p.asString(ctx);
			if ( sep == null ) sep = " ";
		}
		StringBuilder b = new StringBuilder();
		
		ParameterAccumulator[] tkns = TCLEngine.parseLine(new PushbackReader(new StringReader(what)), null);
		int i = 0;
		for ( ParameterAccumulator p : tkns ) {
			String r = p.cheatyResolveOrFizzle().asString();
			if ( i != 0 ) b.append(sep);
			b.append(r);
			++i;
		}

		return new EvaluationResult(Parameter.from(b.toString()));
		
		
	}

}
