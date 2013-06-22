package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.parameters.StringParameter;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;

public class LLength extends TCLCommand {

	
	
	@Override
	public String[] getArguments() {
		return new String[] { "list" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
        Parameter in = ctx.get("list");
        ListParameter list = in.cast(ListParameter.class);
		if ( list == null ) return new EvaluationResult(Parameter.from(1));
		return new EvaluationResult(Parameter.from(list.length()));
	}

}
