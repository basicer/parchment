package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;
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
			if (n < 0) throw new IOException("Unmathced []'s");
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

	public static Parameter evaluate(PushbackReader s, Context ctx) {
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
			return null;
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
