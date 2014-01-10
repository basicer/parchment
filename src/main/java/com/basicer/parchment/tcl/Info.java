package com.basicer.parchment.tcl;


import java.io.PushbackReader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import com.basicer.parchment.*;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.parameters.*;

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

	public Parameter commandsOperation(Parameter dummy, Context ctx, StringParameter pattern) {
		ArrayList<Parameter> result = new ArrayList<Parameter>();
		for ( String s : ctx.getSpellFactory().getAll().keySet() ) {
			if ( pattern != null && !StringCmd.GlobMatch(s, pattern.asString())) continue;;
			result.add(StringParameter.from(s));
		}

		return ListParameter.from(result);
	}

	public Parameter completeOperation(Parameter dummy, Context ctx, StringParameter string) {
		PushbackReader pr = new PushbackReader(new StringReader(string.toString()));
		try {

			TCLEngine.parseLine(pr, null);
		} catch ( FizzleException ex ) {
			return IntegerParameter.from(0);
		}
		return IntegerParameter.from(1);
	}

	public Parameter hostnameOperation(Parameter dummy, Context ctx) {
		try {
			return Parameter.from(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			return Parameter.EmptyString;
		}
	}
	
}
