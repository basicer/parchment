package com.basicer.parchment.tcl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;

public class List extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		StringBuilder result = new StringBuilder();
		int count = 0;
		ArrayList<Parameter> parts = ctx.getArgs();
		for ( int i = 0; i < parts.size(); i++ ) {
			if ( count++ > 0 ) result.append(" ");
			result.append(encode(parts.get(i).asString()));
			++count;
		}
		

		return new EvaluationResult(Parameter.from(result.toString()));
		
		
	}

	public static String encode(String src) {
		boolean needs_brace = false;
		for ( int i = 0; i < src.length(); ++i ) {
			char c = src.charAt(i);
			if ( Character.isWhitespace(c) ) needs_brace = true;
		}
		
		if ( needs_brace ) return "{" + src + "}";
		return src;
		
	}
		
	
}
