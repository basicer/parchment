package com.basicer.parchment.tcl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;

public class Set extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "value" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String name = null;
		Parameter val = ctx.get("value");
		Context ctxu = ctx.up(1);
		String index = null;
		StringBuilder varb = new StringBuilder();
		PushbackReader s = new PushbackReader(new StringReader("$" + ctx.get("varName").asString()));
		
		try { 
			TCLUtils.readVariable(s, varb);
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
		
		//ctx.sendDebugMessage("SET " + name + " ? " + index);
		
		if ( index == null ) {
			if ( val != null ) {
				ctxu.put(name, val);
			}
			
			return new EvaluationResult(ctxu.get(name));	
		}
		
		Parameter p = ctxu.get(name);
		
		if ( p == null ) {
			p = new DictionaryParameter();
			ctxu.put(name, p);
		}
		
		if ( val != null ) { 
			p.writeIndex(index, val);
		}

		return new EvaluationResult(p.index(index));
		
		
	}

}
