package com.basicer.parchment.tcl;

import java.util.ArrayList;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

public class List extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		StringBuilder result = new StringBuilder();
		int count = 0;
		ArrayList<Parameter> parts = ctx.getArgs();
		ListParameter list = ListParameter.from(ctx.getArgs());
		
		return new EvaluationResult(list);
		
		
	}

	public static String encode(String src, boolean first) {
		boolean looks_bracy = false;
		boolean bad_braces = false;
		
		if ( src.length() == 0 ) return "{}";
		
		int openb = 0;
		

		for ( int i = 0; i < src.length(); ++i ) {
			char c = src.charAt(i);
		
			if ( c == '{' ) {
				if ( i == 0 ) looks_bracy = true;
				++openb;
			}
			else if ( c == '}' ) {	
				if ( --openb < 0 ) bad_braces = true;
			}
			else if ( Character.isWhitespace(c) ) looks_bracy = true;
			else if ( c == ';' ) looks_bracy = true;
			else if ( c == '[' ) looks_bracy = true;
			else if ( c == '$' ) looks_bracy = true;
			else if ( c == '#' && i == 0 && first ) looks_bracy = true;
			
		}
		
		if ( openb > 0 ) bad_braces = true;

		if ( bad_braces ) looks_bracy = false;


		if ( looks_bracy ) return "{" + src + "}";

		StringBuilder b = new StringBuilder();
		for ( int i = 0; i < src.length(); ++i ) {
			char c = src.charAt(i);


			if ( c == '\t' ) {
				b.append("\\t");
			} else if ( c == '\n' ) {
				b.append("\\n");
			} else if ( c == '\f' ) {
				b.append("\\f");
			} else if ( c == '\r' ) {
				b.append("\\r");
			} else if ( c == 11 ) {
				b.append("\\v");
			} else if ( c == '#' && first && i == 0) {
				b.append("\\#");
			} else if ( c == '"'  && i == 0) {
				b.append("\\\"");
			} else {
				if ( c == '$' ) b.append('\\');
				else if ( (c == '{' || c == '}') && bad_braces ) { b.append('\\'); }
				else if ( c == '\\' || c == '[' || c == ']' ) b.append('\\');
				b.append(c);
			}
		}


		String out = b.toString();
		
		return out;
		
	}
		
	
}
