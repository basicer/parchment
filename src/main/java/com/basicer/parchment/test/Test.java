package com.basicer.parchment.test;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.fusesource.jansi.Ansi;



import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Test extends TCLCommand {

	public static int tests = 0;
	public static int passed = 0;

	@Override
	public String[] getArguments() { return new String[] { "name", "description", "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		
		String name = ctx.get("name").asString();
		String description = ctx.get("description").asString();
		String body = null;
		Parameter result = null; 
		int resultCode = 0; 
		
		
		ArrayList<Parameter> args = ctx.getArgs();
		if ( args.size() < 1 ) return  EvaluationResult.OK;
		int count = args.size();
		if ( !args.get(0).asString().startsWith("-") ) {
			//Old style
			for ( int i = 0; i < count; ++i ) {
				int backward = count - i - 1;
				if ( i == 0 ) result = args.get(backward);
				else if ( i == 1 ) body = args.get(backward).asString();
				else {
					Parameter constraint = args.get(i - 2);
				}
			}
		} else {
			if ( count % 2 == 1 ) return EvaluationResult.makeError("I dont want to deal with this right now.");
			for ( int i = 0; i < count; i += 2 ) {
				String action = args.get(i).asString();
				if ( !action.startsWith("-") ) return EvaluationResult.makeError("All test options start with -");
				Parameter value = args.get(i+1);
				
				if ( action.equals("-body") ) body = value.asString();
				else if ( action.equals("-result") ) result = value;
			}
		}
		if ( body == null ) EvaluationResult.makeError("All tests need a body.");
		if ( body == null ) EvaluationResult.makeError("All tests need a result.");
		if ( result.asString() == null ) EvaluationResult.makeError("Result wasent a string.");
		
		EvaluationResult testResult = e.evaluate(body, ctx);
		String why = null;
		if ( testResult.getCode().ordinal() != resultCode ) {
			why = String.format("Expcted return code of %d got %d (%s)", resultCode, testResult.getCode().ordinal(), testResult.getValue().asString() );
		} else if ( testResult.getValue() == null ) {
			why = String.format("Expected some return value (%s) but got a real null.", result.asString());
		} else if ( !testResult.getValue().asString().equals(result.asString()) ) {
			why = String.format("Expcted return value of of %s got %s", result.asString(), testResult.getValue().asString() );
		}
		
		++tests;
		if ( why == null ) {
			++passed;
			System.out.println(" + " + name + " " + description + " - Passed " + " \n");
		} else {
			System.out.println(" - " + name + " " + description + " - " + why +  " \n");
		}
		
		
		return EvaluationResult.OK;
	}

	
}
