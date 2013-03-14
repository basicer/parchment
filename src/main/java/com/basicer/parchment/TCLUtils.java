package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class TCLUtils {

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
			else if (c == '{') {
				s.unread(n);
				b.append('{');
				readCurlyBraceString(s, b);
				b.append('}');
				continue;
			}

			if (brackets > 0)
				b.append(c);
		}
	}
	
	public static void readArrayIndex(PushbackReader s, StringBuilder b) throws IOException {
		int brackets = 1;
		if (s.read() != '(')
			throw new IOException("Expected (");
		while (brackets > 0) {
			int n = s.read();
			if (n < 0)
				throw new IOException("Unmathced ()'s");
			char c = (char) n;

			if (c == ')')
				--brackets;
			else if (c == '(')
				++brackets;
			else if (c == '{')
				readCurlyBraceString(s, b);

			if (brackets > 0)
				b.append(c);
		}
	}



	public static void readVariable(PushbackReader s, StringBuilder b) throws IOException {

		if (s.read() != '$')
			throw new IOException("Expected $");
		while (true) {
			int n = s.read();
			if (n < 0)
				break;
			char c = (char) n;
			if (Character.isLetterOrDigit(c) || c == '_' || c == ':' )
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
		if ( !ctx.hasRespectingGlobals(var) ) throw new IllegalArgumentException("Cant resolve variable " + var);
		Parameter p = ctx.getRespectingGlobals(var);
		if ( p == null ) p = Parameter.from("");
		int r = s.read();
		if ( r > 0 ) {
			s.unread(r);
		}
		if ( (char)r == '(' ) {
			StringBuilder b = new StringBuilder();
			readArrayIndex(s, b);
			if ( p == null ) return null;
			return p.index(b.toString());
		}

		// return Parameter.from("[VAR: " + cmd.toString() + "]");
		return p;
	}


	public static String readSlashCode(PushbackReader r) throws IOException {
		int i = r.read();
		if (i < 1)
			return "\\";
		switch ((char) i) {
		case 'n':
			return "\n";
		case 't':
			return "\t";
		case '\n':
			r.unread(' '); //Super Hacky >.<
			return "";
		case '\r':
			int x = r.read();
			if ( x != -1 && (char)x != '\n'  ) r.unread(x);
			r.unread(' '); //Super Hacky >.<
			return "";
		default:
			return "" + (char) i;
		}
	}

	public static Parameter evaluate(String s, Context ctx) {
		TCLEngine e = new TCLEngine(s, ctx);
		while ( e.step() ) {}
		return e.getResult();
		
	}

	public static Parameter evaluate(PushbackReader s, Context ctx) {
		TCLEngine e = new TCLEngine(s, ctx);
		while ( e.step() ) {}
		return e.getResult();
	}

}
