package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Upvar extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "level", "args" }; }


	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		
		Context tctx = ctx.up(1);
		ArrayList<Parameter> args = ctx.getArgs();
		boolean absolute = false;
		String l = ctx.get("level").asString(ctx);
		if ( l.startsWith("#") ) {
			absolute = true;
			l = l.substring(1);
		}

		Integer level = StringParameter.from(l).asInteger();

		Queue<Parameter> argsq = new LinkedList<Parameter>();
		if ( level == null ) {
			argsq.add(ctx.get("level"));
			level = 1;
		}
		for ( Parameter p : args ) argsq.add(p);

		if ( absolute ) {
			level = ctx.getDepth() - level - 1;
		}

		while ( !argsq.isEmpty() ) {
			String target = argsq.poll().asString(ctx);
			String name = argsq.isEmpty() ? target : argsq.poll().asString();
			if ( !tctx.upvar(level, target, name) ) {
				throw new FizzleException("bad level \"" + level + "\"");
			}
		}


		
		
		return EvaluationResult.OK;
	}

}
