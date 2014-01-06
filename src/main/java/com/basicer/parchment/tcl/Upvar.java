package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Upvar extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "level", "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		int level = ctx.get("level").asInteger();
		
		Context tctx = ctx.up(1);
		ArrayList<Parameter> args = ctx.getArgs();
		Queue<Parameter> argsq = new LinkedList<Parameter>();
		for ( Parameter p : args ) argsq.add(p);


		while ( !argsq.isEmpty() ) {
			String target = argsq.poll().asString(ctx);
			String name = argsq.isEmpty() ? target : argsq.poll().asString();
			if ( !tctx.upvar(level, target, name) ) {
				throw new FizzleException("bad level\"" + level + "\"");
			}
		}


		
		
		return EvaluationResult.OK;
	}

}
