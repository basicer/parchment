package com.basicer.parchment.tcl;

import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class StringCmd extends OperationalTCLCommand {

	@Override
	public String getName() { return "string"; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		try {
			
			Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());
			
			Parameter operation = args.poll();
			String op = operation.asString();
			
			if ( op == null ) throw new FizzleException("Operation not a string.");
			if ( op.startsWith("-") ) op = op.substring(1, op.length());
			
			Parameter out = invokeMapped(this, op, args, ctx, null);
			return new EvaluationResult(out);
			
		} catch ( FizzleException ex ) {
			return EvaluationResult.makeError(ex.getMessage());
		}
	}
	
	public Parameter lengthOperation(Parameter dummy, Context ctx, StringParameter str) {
		return Parameter.from(str.asString().length());
	}

	public Parameter indexOperation(Parameter dummy, Context ctx, StringParameter strp, IntegerParameter idx) {
		String str = strp.asString();
		int x = idx.asInteger();
		if ( x < 0 || x >= str.length() ) return Parameter.EmptyString;
		
		return Parameter.from("" + str.charAt(x));
	}
	
	
	//TODO: This need to support flags
	public Parameter equalOperation(Parameter dummy, Context ctx, StringParameter stra, StringParameter strb) {
		
		return Parameter.from(stra.asString().equals(strb.asString()) ? 1 : 0);
	}
	
}
