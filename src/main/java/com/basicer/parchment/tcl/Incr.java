package com.basicer.parchment.tcl;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;

public class Incr extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "varName", "increment?" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String name = null;
		Parameter incr = ctx.get("increment");
		int ammount = 1;
		
		if ( incr != null && incr.asInteger() != null ) ammount = incr.asInteger();
		
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
			Parameter newv = Parameter.from(ctxu.get(name).asInteger() + ammount);
			ctxu.put(name, newv);
			
			return new EvaluationResult(newv);	
		}
		
		Parameter p = ctxu.get(name);
		
		if ( p == null ) {
			p = new DictionaryParameter();
			ctxu.put(name, p);
		}
		
		Parameter newv = Parameter.from(p.get(name).asInteger() + ammount);
		p.writeIndex(name, newv);

		return new EvaluationResult(newv);
		
		
	}

}
