package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class StringCmd extends OperationalTCLCommand {

	@Override
	public String getName() { return "string"; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}


	public static boolean TCLStyleMatch(String target, String pattern) {
		return target.equals(pattern);
	}

	public Parameter lengthOperation(Parameter dummy, Context ctx, StringParameter str) {
		return Parameter.from(str.asString().length());
	}

	public Parameter indexOperation(Parameter dummy, Context ctx, StringParameter strp, IntegerParameter idx) {
		if ( strp == null || idx == null ) throw  new WrongNumberOfArgumentsException();
		String str = strp.asString();
		int x = idx.asInteger();
		if ( x < 0 || x >= str.length() ) return Parameter.EmptyString;
		
		return Parameter.from("" + str.charAt(x));
	}

	@Operation(argnames={"string", "count"})
	public Parameter repeatOperation(Parameter dummy, Context ctx, StringParameter string, IntegerParameter count) {
		if ( string == null || count == null ) throw new WrongNumberOfArgumentsException();

		StringBuilder out = new StringBuilder();
		String text = string.asString(ctx);
		for ( int i = 0; i < count.asInteger(ctx); ++i) {
			out.append(text);
		}
		return Parameter.from(out.toString());
	}
	
	//TODO: This need to support flags
	public Parameter equalOperation(Parameter dummy, Context ctx, StringParameter stra, StringParameter strb) {
		
		return Parameter.from(stra.asString().equals(strb.asString()) ? 1 : 0);
	}

	@Operation(argnames={"string"})
	public Parameter trimOperation(Parameter dummy, Context ctx, StringParameter string) {
		//TODO: Spec has more arguments
		return Parameter.from(string.asString(ctx).trim());

	}

	@Operation(argnames={"string1", "string2"})
	public Parameter firstOperation(Parameter dummy, Context ctx, StringParameter needle, StringParameter haystack ) {
		if ( needle == null || haystack == null ) throw  new WrongNumberOfArgumentsException();
		int idx = haystack.asString(ctx).indexOf(needle.asString(ctx));

		return Parameter.from(idx);
	}

}
