package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

import java.util.ArrayList;

public class Upvar extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "level", "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		int level = ctx.get("level").asInteger();
		
		Context tctx = ctx.up(1);
		ArrayList<Parameter> args = ctx.getArgs();

		for ( int i = 0; i < args.size(); i += 2) {
			String target = args.get(i).asString();
			String name = args.get(i+1) == null ? target : args.get(i+1).asString();
			tctx.upvar(level, target, name);
		}


		
		
		return EvaluationResult.OK;
	}

}
