package com.basicer.parchment.tcl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.*;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Expr extends TCLCommand {

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		String expr = ListParameter.from(ctx.getArgs()).asString(ctx);

		try {
			return new EvaluationResult(eval(expr, ctx, e));
		} catch ( RuntimeException ex ) {
			ex.printStackTrace();
			return EvaluationResult.makeError(ex.getMessage());
		}
	}

	private static String evaluateFunction(String func, String params, Context ctx, TCLEngine e) {

		ArrayList<Parameter> pl = new ArrayList<Parameter>();


		String[] args = params.split(",");
		for ( String p : args ) {
			p = p.trim();
			if (p.length() > 0 ) pl.add(eval(p, ctx, e));
		}




		Parameter out;

		Method method = null;
		try {
			method = Expr.class.getDeclaredMethod(func + "Func", pl.getClass());
		} catch ( NoSuchMethodException ex ) {
			throw new FizzleException("No such expr function: " + func);
		}


		try {
			out = (Parameter)method.invoke(null, pl);
		} catch ( InvocationTargetException ex ) {
			throw new RuntimeException(ex.getTargetException());
		}catch ( IllegalAccessException ex ) {
			throw new RuntimeException(ex);
		}

		return com.basicer.parchment.tcl.List.encode(out.asString(), true);
	}
	
	public static Parameter eval(String expr, Context ctx, TCLEngine e) {
		Debug.info("in Considering %s", expr);
		final Pattern pattern = Pattern.compile("([a-z]+)\\(([^()]*)\\)");
		final Matcher matcher = pattern.matcher(expr);

		while ( matcher.find() ) {
			Debug.info("Found %s", matcher.group(1));
			expr = expr.substring(0, matcher.start()) + evaluateFunction(matcher.group(1), matcher.group(2), ctx, e) + expr.substring(matcher.end());
			matcher.reset(expr);
		}


		PushbackReader s = new PushbackReader(new StringReader(expr));
		Queue<Parameter> tokens = new LinkedList<Parameter>();

		for ( ParameterAccumulator p : e.parseLine(s, ctx, true) ) {
			//Debug.trace("TKN " + p.asString());
			tokens.add(p.cheatyResolveOrFizzle());
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
		Debug.trace("EVAL: %s", (lhs == null ? "null" : lhs.toString()) + " " + op + " " + (rhs == null ? "null" : rhs.toString()));
		
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
			throw new FizzleException("can't use non-numeric string as operand of \"" + op + "\"");
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


	private static Parameter randFunc(ArrayList<Parameter> args) {
		return Parameter.from(StrictMath.random());
	}

	private static Parameter absFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("abs expects 1 arguments");
		double value = args.get(0).asDouble();
		value = Math.abs(value);
		if ( args.get(0) instanceof IntegerParameter ) return IntegerParameter.from(value);
		return DoubleParameter.from(value);
	}

	private static Parameter intFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("int expects 1 arguments");
		double value = args.get(0).asDouble();
		return IntegerParameter.from((int)value);
	}

	private static Parameter roundFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("round expects 1 arguments");
		double value = args.get(0).asDouble();
		return IntegerParameter.from(Math.round(value));
	}

	private static Parameter doubleFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("double expects 1 arguments");
		double value = args.get(0).asDouble();
		return DoubleParameter.from(value);
	}

	private static Parameter sinFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("sin expects 1 arguments");
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.sin(value));
	}

	private static Parameter cosFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("cos expects 1 arguments");
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.cos(value));
	}

	private static Parameter tanFunc(ArrayList<Parameter> args) {
		if ( args.size() != 1 ) throw new FizzleException("tan expects 1 arguments");
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.tan(value));
	}

}
