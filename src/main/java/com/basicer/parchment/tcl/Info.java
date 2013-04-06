package com.basicer.parchment.tcl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class Info extends OperationalTCLCommand {

	
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
	
	public Parameter existsOperation(Parameter dummy, Context ctx, StringParameter str) {
		try {
			Set.access(str.asString(ctx), false, null, ctx.up(1));
			return Parameter.from(true);
		} catch ( FizzleException ex) { 
			return Parameter.from(false);
		}
		
	}

	public Parameter hostnameOperation(Parameter dummy, Context ctx) {
		try {
			return Parameter.from(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			return Parameter.EmptyString;
		}
	}
	
}
