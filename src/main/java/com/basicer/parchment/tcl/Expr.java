package com.basicer.parchment.tcl;

import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.LinkedList;

public class Expr extends TCLCommand {

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Queue<Parameter> q = new LinkedList<Parameter>(ctx.getArgs()); 
		try {
			return new EvaluationResult(parse(q, q.poll(), 0));
		} catch ( RuntimeException ex ) {
			return EvaluationResult.makeError(ex.getMessage());
		}
	}
	
	public static Parameter eval(String expr, Context ctx, TCLEngine e) {
		
		PushbackReader s = new PushbackReader(new StringReader(expr));
		Queue<Parameter> tokens = new LinkedList<Parameter>();
		for ( Parameter p : e.parseLine(s, ctx) ) {
			//Debug.trace("TKN " + p.asString());
			tokens.add(p);
		}
		return parse(tokens, tokens.poll(), 0);
	}
	
	
	//TODO: Doesn't handle ()'s
	public static Parameter parse(Queue<Parameter> tokens, Parameter lhs, int min) {
		while ( true ) {
			if ( tokens.size() < 1 ) {
				if ( lhs instanceof IntegerParameter ) {
					return lhs;
				} else if ( lhs instanceof DoubleParameter ) {
					return ((DoubleParameter) lhs).downCastIfPossible();
				} 
				
				DoubleParameter db = lhs.cast(DoubleParameter.class);
				if ( db != null ) return db.downCastIfPossible();
				
				return lhs;
			}
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
			
			System.err.println(lhs.toString());
		}
	}
	
	public static Parameter evaluate(Parameter lhs, Parameter pop, Parameter rhs) {
		String op = pop.asString();
		Debug.trace("EVAL: " + (lhs == null ? "null" : lhs.toString()) + " " + op + " " + (rhs == null ? "null" : rhs.toString()));
		
		//TODO: Downcasting is not how TCL works, we need to look at the input arguments.
		try {
			if ( op.equals("+") ) return Parameter.from(lhs.asDouble() + rhs.asDouble()).downCastIfPossible();
			if ( op.equals("-") ) return Parameter.from(lhs.asDouble() - rhs.asDouble()).downCastIfPossible();
			if ( op.equals("%") ) return Parameter.from(lhs.asInteger() % rhs.asInteger());
			if ( op.equals("*") ) return Parameter.from(lhs.asDouble() * rhs.asDouble()).downCastIfPossible();
			if ( op.equals("/") ) return Parameter.from(lhs.asDouble() / rhs.asDouble()).downCastIfPossible();	
			if ( op.equals("**") ) return Parameter.from(Math.pow(lhs.asDouble(),rhs.asDouble())).downCastIfPossible();
			
			//TODO: TCL Says > and < work on strings.
			if ( op.equals(">") ) return Parameter.from(lhs.asDouble() > rhs.asDouble());
			if ( op.equals(">=") ) return Parameter.from(lhs.asDouble() >= rhs.asDouble());
			if ( op.equals("<") ) return Parameter.from(lhs.asDouble() < rhs.asDouble());
			if ( op.equals("<=") ) return Parameter.from(lhs.asDouble() <= rhs.asDouble());
			
			if ( op.equals("||") ) return Parameter.from(lhs.asBoolean() || rhs.asBoolean());
			if ( op.equals("&&") ) return Parameter.from(lhs.asBoolean() && rhs.asBoolean());
			
			
			if ( op.equals("eq") ) return Parameter.from(testEquality(lhs,rhs));
			if ( op.equals("ne") ) return Parameter.from(!testEquality(lhs,rhs));
			if ( op.equals("==") ) return Parameter.from(testEquality(lhs,rhs));
			if ( op.equals("!=") ) return Parameter.from(!testEquality(lhs,rhs));
		} catch ( NullPointerException ex ) {
			throw new FizzleException("Some argument couldnt be converted to double.");
		}
		
		throw new FizzleException("No support for " + op);
	}
	
	protected static boolean testEquality(Parameter lhs, Parameter rhs) {
		if ( lhs == null && rhs == null ) return true;
		if ( lhs == null || lhs == null ) return false;
		return lhs.equals(rhs);
	}
	
	private static int getTokenPrecedence(Parameter p) {
		String x = p.asString();
		
		if ( x.length() == 1 ) 
		switch ( x.charAt(0) ) {
			case '+':
			case '-':
				return 12;
			case '*':
			case '/':
			case '%':
				return 13;
			case '<':
			case '>':
				return 10;
			default:
				return -1;
		}
		
		if ( x.equals("**") ) return 14;
		if ( x.equals(">=") || x.equals("<=") ) return 13;
		if ( x.equals("==") || x.equals("!=") ) return 9;
		if ( x.equals("eq") || x.equals("ne") ) return 8;
		if ( x.equals("&&") ) return 3;
		if ( x.equals("||") ) return 2;
		
		return 0;
	}

}
