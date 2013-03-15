package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class TCLEngine {

	PushbackReader sourcecode;
	Context ctx;
	
	public TCLEngine(String src, Context ctx) {
		sourcecode = new PushbackReader(new StringReader(src));
		this.ctx = ctx;
	}
	
	public TCLEngine(PushbackReader src, Context ctx) {
		sourcecode = src;
		this.ctx = ctx;
	}
	
	private EvaluationResult result = null;
	public boolean step() {
		
		while ( true ) {
			Parameter[] pargs = parseLine(sourcecode, ctx);
			if ( pargs == null) return false;
			if ( pargs.length < 1 ) continue;
			//for (Parameter p : pargs) {
			//	ctx.sendDebugMessage("[P] " + p.toString());
			//}
			
			result = evaluate(pargs, ctx);
			if ( result.getCode() != Code.OK ) return false;
			return true;
			//ctx.sendDebugMessage("[R] " + result.toString());
		}

	}

	public EvaluationResult evaluate(String s, Context ctx) {
		return evaluate(new PushbackReader(new StringReader(s)), ctx);
	}
	
	public EvaluationResult evaluate(PushbackReader r, Context ctx) {
		EvaluationResult result = null;
		while ( true ) {
			Parameter[] pargs = parseLine(r, ctx);
			if ( pargs == null) break;
			if ( pargs.length < 1 ) continue;
			//for (Parameter p : pargs) {
			//	ctx.sendDebugMessage("[P] " + p.toString());
			//}
			
			result = evaluate(pargs, ctx);
			if ( result.getCode() != Code.OK ) return result;
			//ctx.sendDebugMessage("[R] " + result.toString());
		}
		return result;
	}


	public EvaluationResult evaluate(Parameter[] pargs, Context ctx) {
		String name = pargs[0].asString();
		TCLCommand s = ctx.getCommand(name);
		if ( s == null ) return EvaluationResult.makeError("No such command: " + name);
		Context c2 = s.bindContext(pargs, ctx);

		return s.extendedExecute(c2, this);		
	}
	
	public Parameter evaulateBracketExpression(PushbackReader s, Context ctx) throws IOException {
		StringBuilder cmd = new StringBuilder();
		TCLUtils.readBracketExpression(s, cmd);

		return evaluate(cmd.toString(), ctx).getValue();
	}
	
	public Parameter[] parseLine(PushbackReader s, Context ctx) {
		List<Parameter> out = new ArrayList<Parameter>();
		StringBuilder current = new StringBuilder();
		char in = '\0';
		boolean empty = true;
		Parameter currentp = null;
		boolean at_end = true; 
		int r;
		try {
			while ((r = s.read()) > 0) {
				char c = (char) r;
				
				boolean append = false;
				if (in == '"') {
					if (c == '"')
						in = '\0';
					else if (c == '{') {
						s.unread(r);
						TCLUtils.readCurlyBraceString(s, current);
						empty = false;
					} else if (c == '[') {
						s.unread(r);
						currentp = evaulateBracketExpression(s, ctx);
					} else if (c == '$') {
						s.unread(r);
						//TODO : We might want to force this to be a string
						Parameter var = TCLUtils.evaulateVariable(s, ctx);
						if ( currentp != null ) { 
							current.append(currentp.asString());
							empty = false;
							currentp = null;
						}
						if ( empty ) currentp = var;
						else current.append(var.asString());
					} else
						append = true;
				} else {
					if (c == '"')
						in = c;
					else if (c == '{') {
						s.unread(r);
						TCLUtils.readCurlyBraceString(s, current);
						empty = false;
					} else if (c == '[') {
						s.unread(r);
						currentp = evaulateBracketExpression(s, ctx);
					} else if (c == ' ' || c == '\t') {
						if (currentp != null) {
							out.add(currentp);
							currentp = null;
						} else if ( !empty ) {
							out.add(Parameter.from(current.toString()));
						}
						current.setLength(0);
						empty = true;
					} else if (c == '\n' || c == ';') {
						at_end = false;
						break;
					} else if ( c == '\r' ) {
						
					} else if (c == '$') {
						s.unread(r);
						Parameter var = TCLUtils.evaulateVariable(s, ctx);
						if ( currentp != null ) { 
							current.append(currentp.asString());
							empty = false;
							currentp = null;
						}
						if ( empty ) currentp = var;
						else current.append(var.asString());
					} else if ( c == '#' && currentp == null && current.length() < 1 ) {
						while ( c != '\n' ) {
							r = s.read();
							if ( r < 0 ) return null;
							c = (char)r;
						}
						return new Parameter[0];
					} else {
						append = true;
					}
				}
				//if (currentp != null && !empty) {
				//	current.append(currentp.asString());
				//	currentp = null;
				//	empty = false;
				//}
				if (append) {
					if (currentp != null) {
						current.append(currentp.asString(ctx));
						currentp = null;
					}
					if (c == '\\')
						current.append(TCLUtils.readSlashCode(s));
					else
						current.append(c);
					
					empty = false;
				}
				
			}
			
			if (currentp != null && current.length() == 0) {
				out.add(currentp);
			} else if (!empty) {
				if (currentp != null) 
					current.append(currentp.asString(ctx));
				out.add(Parameter.from(current.toString()));
			} 
			
			
			
		} catch (IOException e) {
			throw new Error(e);
		}

		if ( at_end && out.size() < 1 ) return null;
		return out.toArray(new Parameter[0]);
	}

	public Parameter getResult() {
		// TODO Auto-generated method stub
		if ( result == null ) return null;
		return result.getValue();
	}
	
	public Code getCode() {
		// TODO Auto-generated method stub
		if ( result == null ) return null;
		return result.getCode();
	}

	
}
