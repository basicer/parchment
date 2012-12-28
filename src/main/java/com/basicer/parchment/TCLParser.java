package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.parameters.Parameter;

public class TCLParser {

	public static void readCurlyBraceString(PushbackReader s, StringBuilder b) throws IOException {
		int brackets = 1;
		if (s.read() != '{')
			throw new IOException("Expected {");
		while (brackets > 0) {
			int n = s.read();
			if (n < 0)
				throw new IOException("Unmathced {}'s");
			char c = (char) n;

			if (c == '}')
				--brackets;
			else if (c == '{')
				++brackets;

			if (brackets > 0)
				b.append(c);

		}
	}

	public static void readBracketExpression(PushbackReader s, StringBuilder b) throws IOException {
		int brackets = 1;
		if (s.read() != '[')
			throw new IOException("Expected [");
		while (brackets > 0) {
			int n = s.read();
			if (n < 0)
				throw new IOException("Unmathced []'s");
			char c = (char) n;

			if (c == ']')
				--brackets;
			else if (c == '[')
				++brackets;
			else if (c == '{')
				readCurlyBraceString(s, b);

			if (brackets > 0)
				b.append(c);
		}
	}

	public static Parameter evaulateBracketExpression(PushbackReader s, Context ctx) throws IOException {
		StringBuilder cmd = new StringBuilder();
		readBracketExpression(s, cmd);

		return evaluate(cmd.toString(), ctx);
	}

	public static void readVariable(PushbackReader s, StringBuilder b) throws IOException {

		if (s.read() != '$')
			throw new IOException("Expected $");
		while (true) {
			int n = s.read();
			if (n < 0)
				break;
			char c = (char) n;
			if (Character.isLetterOrDigit(c) || c == '_')
				b.append(c);
			else {
				s.unread(n);
				return;
			}

		}
	}

	public static Parameter evaulateVariable(PushbackReader s, Context ctx) throws IOException {
		StringBuilder varb = new StringBuilder();
		readVariable(s, varb);
		String var = varb.toString();
		Parameter p = ctx.getRespectingGlobals(var);
		// return Parameter.from("[VAR: " + cmd.toString() + "]");
		return p;
	}

	
	
	public static Parameter evaluate(String cmd, Context ctx) {
		PushbackReader r = new PushbackReader(new StringReader(cmd));
		return evaluate(r, ctx);
	}
	
	public static Parameter evaluate(PushbackReader r, Context ctx) {
		Parameter result = null;
		
		while ( true ) {
			Parameter[] pargs = TCLParser.parseLine(r, ctx);
			if ( pargs == null) break;
			if ( pargs.length < 1 ) continue;
			//for (Parameter p : pargs) {
			//	ctx.sendDebugMessage("[P] " + p.toString());
			//}

			result = evaluate(pargs, ctx);
			//ctx.sendDebugMessage("[R] " + result.toString());
		}
		return result;
	}

	public static Parameter evaluate(Parameter[] pargs, Context ctx) {
		String name = pargs[0].asString();
		TCLCommand s = ctx.getCommand(name);
		if ( s == null ) throw new Error("No such command: " + name);
		Context c2 = s.bindContext(pargs, ctx);
		return s.execute(c2);
	}

	public static Parameter[] parseLine(PushbackReader s, Context ctx) {
		List<Parameter> out = new ArrayList<Parameter>();
		StringBuilder current = new StringBuilder();
		char in = '\0';
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
						readCurlyBraceString(s, current);
					} else if (c == '[') {
						s.unread(r);
						currentp = evaulateBracketExpression(s, ctx);
					} else if (c == '$') {
						s.unread(r);
						currentp = evaulateVariable(s, ctx);
					} else
						append = true;
				} else {
					if (c == '"')
						in = c;
					else if (c == '{') {
						s.unread(r);
						readCurlyBraceString(s, current);
					} else if (c == '[') {
						s.unread(r);
						currentp = evaulateBracketExpression(s, ctx);
					} else if (c == ' ' || c == '\t') {
						if (currentp != null) {
							out.add(currentp);
							currentp = null;
						} else if ( current.length() > 0) {
							out.add(Parameter.from(current.toString()));
						}
						current.setLength(0);
					} else if (c == '\n' || c == ';') {
						at_end = false;
						break;
					} else if ( c == '\r' ) {
						
					} else if (c == '$') {
						s.unread(r);
						currentp = evaulateVariable(s, ctx);
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
				if (currentp != null && current.length() > 0) {
					current.append(currentp.asString());
					currentp = null;
				}
				if (append) {
					if (currentp != null) {
						current.append(currentp.asString());
						currentp = null;
					}
					if (c == '\\')
						current.append(translateSlashCode(s.read()));
					else
						current.append(c);
				}
			}
			if (current.length() > 0) {
				out.add(Parameter.from(current.toString()));
			} else if (currentp != null) {
				out.add(currentp);
			}
		} catch (IOException e) {
			throw new Error(e);
		}

		if ( at_end && out.size() < 1 ) return null;
		return out.toArray(new Parameter[0]);
	}

	private static char translateSlashCode(int i) {
		if (i < 1)
			return '\\';
		switch ((char) i) {
		case 'n':
			return '\n';
		case 't':
			return 'X';
		default:
			return (char) i;
		}
	}

}
