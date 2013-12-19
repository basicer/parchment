package com.basicer.parchment.tcl;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;
import com.basicer.parchment.*;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
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

			return invokeMapped(this, op, args, ctx, null);

			
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

	public Parameter versionOperation(Parameter dummy, Context ctx) {
		return StringParameter.from(ParchmentPluginLite.instance().getDescription().getVersion());
	}

	public Parameter hostnameOperation(Parameter dummy, Context ctx) {
		try {
			return Parameter.from(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			return Parameter.EmptyString;
		}
	}
	
}
