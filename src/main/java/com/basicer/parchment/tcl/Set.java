package com.basicer.parchment.tcl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;

public class Set extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "newValue?" }; }
	
	
	public static Parameter access(String varName, boolean write, Parameter value, Context ctxu) {
		PushbackReader s = new PushbackReader(new StringReader("$" + varName));
		String name = null;
		String index = null;
		StringBuilder varb = new StringBuilder();

		try { 
			TCLUtils.readVariableInHand(s, varb);
			name = varb.toString();
			varb = new StringBuilder();			
			
			int r = s.read();
			if ( (char)r == '(' ) {
				s.unread(r);
				TCLUtils.readArrayIndex(s, varb);
				index = varb.toString();
			}
		} catch ( IOException ex ) {
			
		}
		
		//Debug.trace("%s to %s (%s) = %s", write ? "Write" : "read",  name, index, value); 
		
		if ( index == null ) {
			if ( write ) {
				if ( value == null ) ctxu.unset(name); 
				else ctxu.put(name, value);
			} else {
				if ( ctxu.getRespectingGlobals(name) == null ) throw new FizzleException("can't read \"" + name + "\": no such variable");
			}
			
			
			return ctxu.getRespectingGlobals(name);	
		}
		
		Parameter p = ctxu.getRespectingGlobals(name);
		
		if ( p == null && write) {
			p = new DictionaryParameter();
			ctxu.put(name, p);
		} else if ( p == null ){
			throw new FizzleException("can't read \"" + name + "(" + index + ")\": no such element in array");
		} else if ( !(p instanceof DictionaryParameter) ) {
			throw new FizzleException("can't read \"" + name + "(" + index + ")\": variable isn't array");
		}
		
		if ( write ) {
			if ( value == null ) p.deleteIndex(name); 
			else p.writeIndex(index, value);
		} else if ( p.index(index) == null) {
			throw new FizzleException("can't read \"" + name + "(" + index + ")\": no such element in array");
		}
		
		return p.index(index);
	}
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		Parameter val = ctx.get("newValue");
		Context ctxu = ctx.up(1);
		return new EvaluationResult(access(ctx.get("varName").asString(), val != null,  val, ctxu));
				
	}

}
