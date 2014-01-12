package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.tcl.Set;
import com.basicer.parchment.tclstrings.ErrorStrings;

public class TCLUtils {

	public static void readCurlyBraceString(PushbackReader s, StringBuilder b) throws IOException {
		int brackets = 1;
		if (s.read() != '{') throw new IOException("Expected {");
		boolean inEscape = false;
		while (brackets > 0) {
			int n = s.read();
			if (n < 0) throw new FizzleException(ErrorStrings.BraceMismatch);
			char c = (char) n;

			if ((c == '\n' || c == '\r') && inEscape) {
				int ns = s.read();
				while (ns > 0 && Character.isWhitespace((char) ns))
					ns = s.read();
				s.unread(ns);
				b.append(" ");
				inEscape = false;
				continue;
			}

			if (!inEscape) {
				if (c == '}') {
					--brackets;
				} else if (c == '{') {
					++brackets;
				} else if (c == '\\') {
					inEscape = true;
					continue;
				} 
			} else {
				inEscape = false;
				b.append("\\");
			}

			if (brackets > 0) b.append(c);

		}
	}

	public static void readBracketExpression(PushbackReader s, StringBuilder b) throws IOException {
		int brackets = 1;
		if (s.read() != '[') throw new IOException("Expected [");
		boolean quotes = false;
		while (brackets > 0) {
			int n = s.read();
			if (n < 0) throw new IOException("missing close-bracket");
			char c = (char) n;

			if ( !quotes ) {
				if (c == ']')
					--brackets;
				else if (c == '[')
					++brackets;
				else if ( c == '"')
					quotes = true;
				else if (c == '{') {
					s.unread(n);
					b.append('{');
					readCurlyBraceString(s, b);
					b.append('}');
					continue;
				}
			} else {
				if ( c == '"' ) quotes = false; //TODO: Need more logic here, for \'s and stuf
			}

			if (brackets > 0) b.append(c);
		}
	}

	public static void readArrayIndex(PushbackReader s, StringBuilder b, Context ctx) throws IOException {
		int brackets = 1;
		if (s.read() != '(') throw new IOException("Expected (");
		while (brackets > 0) {
			int n = s.read();
			if (n < 0) throw new IOException("Unmathced ()'s");
			char c = (char) n;

			if (c == ')')
				--brackets;
			else if (c == '(')
				++brackets;
			else if (c == '{')
				readCurlyBraceString(s, b);
			else if (c == '$') {
				s.unread(n);
				b.append(TCLUtils.evaulateVariable(s, ctx).asString());
				continue;
			}
			if (brackets > 0) b.append(c);
		}
	}

	public static void readVariable(PushbackReader s, StringBuilder b) throws IOException {

		if (s.read() != '$') throw new IOException("Expected $");

		int i = 0;
		while (true) {
			int n = s.read();
			if (n < 0) break;
			char c = (char) n;
			if (c == '{') { // TODO: The variable needs to start with this so,
							// but checking for that is worse unit test wise.
				s.unread(c);
				readCurlyBraceString(s, b);
				return;
			} else if (Character.isLetterOrDigit(c) || c == '_' || c == ':')
				b.append(c);
			else {
				s.unread(n);
				return;
			}
			++i;
		}
	}

	public static void readVariableInHand(PushbackReader s, StringBuilder b) throws IOException {

		if (s.read() != '$') throw new IOException("Expected $");

		while (true) {
			int n = s.read();
			if (n < 0) break;
			char c = (char) n;
			if (c != '(') {
				b.append(c);
			} else {
				s.unread(n);
				return;
			}

		}
	}

	/*
	 * public static Parameter evaulateVariable(PushbackReader s, Context ctx)
	 * throws IOException { StringBuilder varb = new StringBuilder();
	 * readVariable(s, varb); String var = varb.toString(); if (
	 * !ctx.hasRespectingGlobals(var) ) throw new
	 * FizzleException("can't read \"" + var +"\": no such variable"); Parameter
	 * p = ctx.getRespectingGlobals(var); if ( p == null ) p =
	 * Parameter.from(""); int r = s.read(); if ( r > 0 ) { s.unread(r); } if (
	 * (char)r == '(' ) { StringBuilder b = new StringBuilder();
	 * readArrayIndex(s, b); if ( p == null ) return null; return
	 * p.index(b.toString()); }
	 * 
	 * // return Parameter.from("[VAR: " + cmd.toString() + "]"); return p; }
	 */

	public static Parameter evaulateVariable(PushbackReader s, Context ctx) throws IOException {
		String var = readVariableName(s, ctx);
		return Set.access(var, false, null, ctx);
	}

	public static String readVariableName(PushbackReader s, Context ctx) throws IOException {
		StringBuilder varb = new StringBuilder();
		readVariable(s, varb);
		String var = varb.toString();

		int r = s.read();
		if (r > 0) {
			s.unread(r);
		}
		if ((char) r == '(') {
			StringBuilder b = new StringBuilder();
			readArrayIndex(s, b, ctx);
			return var + "(" + b.toString() + ")";
		}

		// return Parameter.from("[VAR: " + cmd.toString() + "]");
		return var;
	}

	public static ParameterAccumulator[] parseLine(PushbackReader s, Context ctx, boolean expr) {
		ArrayList<ParameterAccumulator> out = new ArrayList<>();
		boolean first = true;
		do {
			try {
				ParameterAccumulator read = parseWord(s, ctx, first);
				if ( read == null ) {
					if ( read == null && out.size() == 0 ) return null;
//					for ( int i = 0; i < out.size(); ++i ) {
//						System.out.println(" " + i + " :" + out.get(i).toString());
//					}
					return out.toArray(new ParameterAccumulator[0]);
				}
				out.add(read);
			} catch ( IOException ex ) {
				throw new FizzleException(ex.getMessage());
			}
			first = false;
		} while ( true );


	}

	public static ParameterAccumulator parseWord(PushbackReader s, Context ctx, boolean first) throws IOException {
		return parseWord(s, ctx, first, "");
	}
	public static ParameterAccumulator parseWord(PushbackReader s, Context ctx, boolean first, String extraStop) throws IOException {
		char in = '\0';
		ParameterAccumulator current = new ParameterAccumulator();

		int r;
		while ( (r = s.read()) > 0 ) {
			char c = (char) r;
			boolean append = false;
			if ( in == '"' ) {
				if ( c == '\\' ) {
					current.append(TCLUtils.readSlashCode(s));
				} else if ( c == '"' ) {
					in = '\0';
					int xcn = s.read();
					if ( xcn > 0 ) {
						int xcnn = s.read();
						if ( xcnn > 0 ) s.unread(xcnn);
						s.unread(xcn);
						//If right after a close quote we try to eat a new line, thats okay.
						//TODO: This wount throw the correct error for something like "\\
						if ( xcn == '\\' ) xcn = xcnn;
						if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
							if ( extraStop.indexOf(xcn) == -1 ) throw new FizzleException("extra characters after close-quote");
						}
						return current;

					}
				} else if ( c == '[' ) {
					s.unread(r);
					current.append(ParameterAccumulator.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
				} else if ( c == '$' ) {
					s.unread(r);
					String name = TCLUtils.readVariableName(s, ctx);
					if ( name.length() > 0 ) {
						current.append(ParameterAccumulator.Type.VARIABLE, name, ctx);
					} else {
						append = true;
					}
				} else {
					append = true;
				}
			} else {
				if ( c == '\\' ) {
					String ta = TCLUtils.readSlashCode(s);
					if ( !ta.equals("") ) {
						current.append(ta);
						continue;
					} else {
						c = ' ';
					}
				}
				if ( !current.empty() && extraStop.indexOf(c) != -1 ) {
					s.unread(c);
					return current;
				}
				else if ( c == '"' && current.empty() ) {
					in = c;
				} else if ( c == '{' && current.empty() ) {
					s.unread(r);
					current.append(TCLUtils.readCurlyBraceStringToString(s));
					int xcn = s.read();
					if ( xcn > 0 ) {
						s.unread(xcn);
						if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
							throw new FizzleException("extra characters after close-brace");
						}
					}
				} else if ( c == '[' ) {
					s.unread(r);
					current.append(ParameterAccumulator.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
				} else if ( c == ' ' || c == '\t' || c == '\r' || c == (char) 11 || c == '\f' ) {
					if ( !current.empty() ) return current;
				} else if ( c == '\n' || c == ';' ) {
					if ( !current.empty() ) {
						s.unread(c);
						return current;
					} else if ( !first ) {
						return null;
					}
				} else if ( c == '$' ) {
					s.unread(r);
					String name = TCLUtils.readVariableName(s, ctx);
					if ( name.length() > 0 ) {
						current.append(ParameterAccumulator.Type.VARIABLE, name, ctx);
					} else {
						append = true;
					}
				} else if ( c == '#' && current.empty() && first ) {
					boolean before_slash = false;

					while ( c != '\n' || before_slash ) {
						r = s.read();
						if ( r < 0 ) return parseWord(s, ctx,  first);

						//Account for an escaped endline, which continues the comment
						if ( before_slash ) before_slash = false;
						else if ( c == '\\' ) before_slash = true;
						c = (char) r;
					}
					return parseWord(s, ctx, first);
				} else {
					append = true;
				}
			}

			if ( append ) current.append("" + c);

		}

		if ( in != '\0' ) throw new FizzleException("missing " + in);
		if ( !current.empty() ) return current;
		return null;
	}

	public static String readSlashCode(PushbackReader r) throws IOException {
		int i = r.read();
		if (i < 0) return "\\";
		switch ((char) i) {
		case 'n':
			return "\n";
		case 'r':
			return "\r";
		case 'f':
			return "\f";
		case 'v':
			return "" + (char) 11;
		case 't':
			return "\t";
		case '\n':
		case '\r':
			int ns = r.read();
			while (ns > 0 && Character.isWhitespace((char) ns))
				ns = r.read();
			r.unread(ns);
			return "";
		case 'u':
			String hex = "";
			;
			for (int hi = 0; hi < 4; ++hi) {
				int ic = r.read();
				if ( ic < 0 ) break;
				char c = (char)ic;
				if (!"abcdefABCDEF0123456789".contains("" + c)) break;
				hex += c;

			}
			if (hex.length() < 1) return "u";
			try {
				return "" + (char) (Integer.valueOf(hex, 16).intValue());
			} catch (NumberFormatException ex) {
				throw new FizzleException("Invalid unicode codepoint:" + hex);
			}
		case 'x':
			String hex1 = "";
			;
			for (int hi = 0; hi < 2; ++hi) {
				int ic = r.read();
				if ( ic < 0 ) break;
				char c = (char)ic;
				if (!"abcdefABCDEF0123456789".contains("" + c)) { r.unread(ic); break; }
				hex1 += c;
			}
			if (hex1.length() < 1) return "x";
			try {
				return "" + (char) (Integer.valueOf(hex1, 16).intValue());
			} catch (NumberFormatException ex) {
				throw new FizzleException("Invalid unicode codepoint:" + hex1);
			}
		default:
			return "" + (char) i;
		}
	}

	public static Parameter evaluate(String s, Context ctx) {
		TCLEngine e = new TCLEngine(s, ctx);
		while (e.step()) {
		}
		return e.getResult();

	}

	public static Number parseStringToNumber(String str) {
		if ( str.startsWith("0o") ) {
			str = "0" + str.substring(2);
		} else if ( str.startsWith("-0o") ) {
			str = "-0" + str.substring(3);
		} else if ( str.startsWith("+0o") ) {
			str = "0" + str.substring(3);
		}

		try { return Long.decode(str); }
		catch ( NumberFormatException ex ) { }

		try { return Double.parseDouble(str); }
		catch ( NumberFormatException ex ) { }

		return null;
	}

	public static Parameter evaluate(Reader s, Context ctx) {
		TCLEngine e = new TCLEngine(s, ctx);
		while (e.step()) {
		}
		if (e.getCode() == Code.ERROR) throw new RuntimeException("TCLError: " + e.getResult().asString());
		return e.getResult();
	}

	public static String readBracketExpressionToString(PushbackReader s) {

		StringBuilder b = new StringBuilder();
		try {
			readBracketExpression(s, b);
		} catch (IOException e) {
			throw new FizzleException(e.getMessage());
		}
		return b.toString();

	}

	public static String readCurlyBraceStringToString(PushbackReader s) {
		StringBuilder b = new StringBuilder();
		try {
			readCurlyBraceString(s, b);
		} catch (IOException e) {
			return null;
		}
		return b.toString();

	}
	
}
