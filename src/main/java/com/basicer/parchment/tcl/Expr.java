package com.basicer.parchment.tcl;

import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLParser;
import com.basicer.parchment.parameters.Parameter;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.LinkedList;

public class Expr extends TCLCommand {

	@Override
	public Parameter execute(Context ctx) {
		Queue<Parameter> q = new LinkedList<Parameter>(ctx.getArgs()); 
		return parse(q, q.poll(), 0);
	}
	
	public static Parameter eval(String expr, Context ctx) {
		
		PushbackReader s = new PushbackReader(new StringReader(expr));
		Queue<Parameter> tokens = new LinkedList<Parameter>();
		for ( Parameter p : TCLParser.parseLine(s, ctx) ) tokens.add(p);
		return parse(tokens, tokens.poll(), 0);
	}
	
	public static Parameter parse(Queue<Parameter> tokens, Parameter lhs, int min) {
		while ( true ) {
			if ( tokens.size() < 1 ) return lhs;
			Parameter op = tokens.peek();
			if ( getTokenPrecedence(op) < min ) return lhs;
			tokens.poll();
			
			Parameter rhs = tokens.poll();
			
			while ( true ) {
				Parameter lookahead = tokens.peek();
				if ( lookahead == null || getTokenPrecedence(lookahead) <= getTokenPrecedence(op) ) {
					break;
				}
				
				rhs = parse(tokens, rhs, getTokenPrecedence(lookahead));
			}
			
			lhs = evaluate(lhs, op, rhs);
		}
	}
	
	public static Parameter evaluate(Parameter lhs, Parameter pop, Parameter rhs) {
		String op = pop.asString();
		System.out.println(" " + lhs.toString() + " " + op + rhs.toString());
		
		if ( op.equals("+") ) return Parameter.from(lhs.asDouble() + rhs.asDouble());
		if ( op.equals("+") ) return Parameter.from(lhs.asDouble() - rhs.asDouble());
		if ( op.equals("%") ) return Parameter.from(lhs.asInteger() % rhs.asInteger());
		if ( op.equals("*") ) return Parameter.from(lhs.asDouble() * rhs.asDouble());
		if ( op.equals("/") ) return Parameter.from(lhs.asDouble() / rhs.asDouble());
		if ( op.equals("eq") ) return Parameter.from(lhs.equals(rhs));
		if ( op.equals("ne") ) return Parameter.from(!lhs.equals(rhs));
		if ( op.equals("==") ) return Parameter.from(lhs.equals(rhs));
		if ( op.equals("!=") ) return Parameter.from(!lhs.equals(rhs));
		
		return null;
	}
	
	private static int getTokenPrecedence(Parameter p) {
		String x = p.asString();
		
		if ( x.length() == 1 ) 
		switch ( x.charAt(0) ) {
			case '+':
			case '-':
				System.out.println("OP " + x + " 1");
				return 12;
			case '*':
			case '/':
			case '%':
				System.out.println("OP " + x + " 2");
				return 13;
			default:
				System.out.println("OP " + x + "");
				return -1;
		}
		
		if ( x.equals("**") ) return 14;
		if ( x.equals("==") || x.equals("!=") ) return 9;
		
		System.out.println("OP |" + x + "| XXX");
		return 0;
	}

}
