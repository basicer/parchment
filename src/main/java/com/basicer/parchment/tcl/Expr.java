package com.basicer.parchment.tcl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.*;
import com.comphenix.protocol.reflect.FuzzyReflection;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Expr extends TCLCommand {

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		String expr = Concat.doConcat(ctx.getArgs()).asString(ctx);

		try {
			return new EvaluationResult(eval(expr, ctx.up(1), e));
		} catch (FizzleException fex ) {
			throw fex;
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
			if ( out instanceof DoubleParameter ) {
				double d = out.asDouble().doubleValue();
				boolean down_convert = (d == Math.floor(d));
				for ( int i = 0; i < pl.size(); ++i )if ( !(pl.get(i) instanceof IntegerParameter) ) down_convert = false;
				if ( down_convert ) out = IntegerParameter.from((long) d);
			}
		} catch ( InvocationTargetException ex ) {
			if ( ex.getTargetException() instanceof RuntimeException ) throw (RuntimeException)ex.getTargetException();
			else throw new RuntimeException(ex.getTargetException());
		} catch ( IllegalAccessException ex ) {
			throw new RuntimeException(ex);
		}

		return com.basicer.parchment.tcl.List.encode(out.asString(), true);
	}

	private static final Pattern fx_pattern = Pattern.compile("([a-z][a-z0-9]*)\\(([^()]*)\\)");
	public static Parameter eval(String expr, Context ctx, TCLEngine e) {
		Debug.info("in Considering %s", expr);
		final String exprSymbols = "-+*/%=<>^&|!";

		final Matcher matcher = fx_pattern.matcher(expr);

		while ( matcher.find() ) {
			Debug.info("Found %s", matcher.group(1));
			try {
				expr = expr.substring(0, matcher.start()) + evaluateFunction(matcher.group(1), matcher.group(2), ctx, e) + expr.substring(matcher.end());
			} catch ( Exception ex ) {
				throw new FizzleException(ex.getMessage());
			}
			matcher.reset(expr);
		}


		PushbackReader s = new PushbackReader(new StringReader(expr), 2);
		Queue<Parameter> tokens = new LinkedList<Parameter>();

		boolean operator = false;
		do {
			try {
				ParameterAccumulator read = null;
				if ( operator ) {


					do {
						int r = s.read();
						if ( r < 0 ) break;
						if ( Character.isWhitespace((char) r) ) continue;
						if ( exprSymbols.indexOf((char) r) == -1 ) {
							s.unread(r);
							break;
						}
						if ( read == null ) read = new ParameterAccumulator();
						read.append("" + (char)r);
					} while ( true );
					operator = false;
				} else {
					read = TCLEngine.parseWord(s, ctx, false, exprSymbols);
					operator = true;
				}
				if ( read == null ) break;
				//System.out.println("--> " + read.toString() );
				tokens.add(read.cheatyResolveOrFizzle());
			} catch (IOException ex) {
				break;
			}

		} while ( true );


		Parameter p = parse(tokens, tokens.poll(), 0);
		try {
			if ( p == null ) throw new FizzleException("Null during expr");
		} catch ( Exception ex ) {
			throw new FizzleException(ex.getMessage());
		}
		return p;

	}
	
	
	//TODO: Doesn't handle ()'s
	public static Parameter parse(Queue<Parameter> tokens, Parameter lhs, int min) {
		while ( true ) {
			if ( tokens.size() < 1 ) {
				Parameter n = lhs.makeNumeric();
				if ( n != null ) return n;
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

			boolean downcast = hasIntPrecision(lhs) && hasIntPrecision(rhs);
			lhs = evaluate(lhs, op, rhs);
			if ( downcast ) {
				Parameter n = IntegerParameter.from(lhs.asInteger());
				if ( n != null ) lhs = n;
			}

			Debug.trace("%s",lhs.toString());
		}
	}

	public static boolean hasIntPrecision(Parameter p) {
		if ( p == null ) return false;
		if ( p instanceof DoubleParameter ) return false;
		if ( p instanceof IntegerParameter ) return true;
		try {
			Number n = TCLUtils.parseStringToNumber(p.asString());
			if ( n == null ) return false;
			return ( n instanceof Long );
		} catch ( NumberFormatException ex ) {
			return false;
		}
	}

	public static boolean isNumeric(Parameter p) {
		return p.asDouble() != null;
	}



	public static int compare(Parameter lhs, Parameter rhs) {
		boolean lhs_numeric = isNumeric(lhs);
		boolean rhs_numeric = isNumeric(rhs);

		if ( lhs_numeric && rhs_numeric ) return lhs.asDouble().compareTo(rhs.asDouble());
		return lhs.asString().compareTo(rhs.asString());
	}

	public static Parameter evaluate(Parameter lhs, Parameter pop, Parameter rhs) {
		String op = pop.asString();
		Debug.trace("EVAL: %s", (lhs == null ? "null" : lhs.toString()) + " " + op + " " + (rhs == null ? "null" : rhs.toString()));

		try {

			if ( op.equals(">") ) return Parameter.from(compare(lhs,rhs) > 0 ? 1 : 0);
			if ( op.equals(">=") ) return Parameter.from(compare(lhs,rhs) >= 0 ? 1 : 0);
			if ( op.equals("<") ) return Parameter.from(compare(lhs,rhs) < 0 ? 1 : 0);
			if ( op.equals("<=") ) return Parameter.from(compare(lhs, rhs) <= 0 ? 1 : 0);
			if ( op.equals("eq") ) return Parameter.from(testEquality(lhs,rhs) ? 1 : 0);
			if ( op.equals("ne") ) return Parameter.from(!testEquality(lhs,rhs) ? 1 : 0);
			if ( op.equals("==") ) return Parameter.from(testEquality(lhs,rhs) ? 1 : 0);
			if ( op.equals("!=") ) return Parameter.from(!testEquality(lhs,rhs) ? 1 : 0);

			if ( !isNumeric(lhs) || !isNumeric(rhs) ) throw new FizzleException("can't use non-numeric string as operand of \"" + op + "\"");
			if ( op.equals("+") ) return Parameter.from(lhs.asDouble() + rhs.asDouble());
			if ( op.equals("-") ) return Parameter.from(lhs.asDouble() - rhs.asDouble());

			if ( op.equals("*") ) return Parameter.from(lhs.asDouble() * rhs.asDouble());
			if ( op.equals("/") ) return Parameter.from(lhs.asDouble() / rhs.asDouble());
			if ( op.equals("**") ) return Parameter.from(Math.pow(lhs.asDouble(), rhs.asDouble()));

			if ( !hasIntPrecision(lhs) || !hasIntPrecision(rhs) ) throw new FizzleException("can't use floating-point value as operand of \"" + op + "\"");
			if ( op.equals("%") ) return Parameter.from(lhs.asInteger() % rhs.asInteger());
			if ( op.equals("<<") ) return Parameter.from(lhs.asLong() << rhs.asLong());
			if ( op.equals(">>") ) return Parameter.from(lhs.asLong() >> rhs.asLong());



			if ( op.equals("||") ) return Parameter.from((lhs.asBoolean() || rhs.asBoolean()) ? 1 : 0 );
			if ( op.equals("&&") ) return Parameter.from((lhs.asBoolean() && rhs.asBoolean()) ? 1 : 0 );
			
			

		} catch ( Exception ex ) {
			throw new FizzleException(ex.getMessage());
		}
		
		throw new FizzleException("No support for " + op);
	}
	
	protected static boolean testEquality(Parameter lhs, Parameter rhs) {
		if ( lhs == null && rhs == null ) return true;
		if ( lhs == null || rhs == null ) return false;
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

	private static void checkArgumentSize( String func, ArrayList<Parameter> args, int n) {
		if ( args.size() < n ) throw new FizzleException("too few arguments for math function \"" + func + "\"");
		if ( args.size() > n ) throw new FizzleException("too many arguments for math function \"" + func + "\"");
	}

	private static Random randomNumberGenerator;
	private static Parameter randFunc(ArrayList<Parameter> args) {
		checkArgumentSize("rand", args,0);
		if ( randomNumberGenerator == null ) randomNumberGenerator = new Random();
		return Parameter.from(randomNumberGenerator.nextDouble());
	}

	private static Parameter srandFunc(ArrayList<Parameter> args) {
		checkArgumentSize("srand", args,1);
		randomNumberGenerator = new Random(args.get(0).asLong());
		return Parameter.from(randomNumberGenerator.nextDouble());
	}

	private static Parameter absFunc(ArrayList<Parameter> args) {
		checkArgumentSize("abs", args,1);
		double value = args.get(0).asDouble();
		value = Math.abs(value);
		return DoubleParameter.from(value);
	}

	private static Parameter intFunc(ArrayList<Parameter> args) {
		checkArgumentSize("int", args,1);
		double value = args.get(0).asDouble();
		return IntegerParameter.from((long)value);
	}

	private static Parameter roundFunc(ArrayList<Parameter> args) {
		checkArgumentSize("round", args,1);
		double value = args.get(0).asDouble();
		return IntegerParameter.from(Math.round(value));
	}

	private static Parameter floorFunc(ArrayList<Parameter> args) {
		checkArgumentSize("floor", args,1);
		double value = args.get(0).asDouble();
		return IntegerParameter.from(Math.floor(value));
	}

	private static Parameter ceilFunc(ArrayList<Parameter> args) {
		checkArgumentSize("ceil", args,1);
		double value = args.get(0).asDouble();
		return IntegerParameter.from(Math.ceil(value));
	}

	private static Parameter doubleFunc(ArrayList<Parameter> args) {
		checkArgumentSize("double", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(value);
	}

	private static Parameter sinFunc(ArrayList<Parameter> args) {
		checkArgumentSize("sin", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.sin(value));
	}

	private static Parameter cosFunc(ArrayList<Parameter> args) {
		checkArgumentSize("cos", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.cos(value));
	}

	private static Parameter tanFunc(ArrayList<Parameter> args) {
		checkArgumentSize("tan", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.tan(value));
	}

	private static Parameter asinFunc(ArrayList<Parameter> args) {
		checkArgumentSize("asin", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.asin(value));
	}

	private static Parameter acosFunc(ArrayList<Parameter> args) {
		checkArgumentSize("acos", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.acos(value));
	}

	private static Parameter atanFunc(ArrayList<Parameter> args) {
		checkArgumentSize("atan", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.atan(value));
	}

	private static Parameter atan2Func(ArrayList<Parameter> args) {
		checkArgumentSize("atan2", args,2);
		double value1 = args.get(0).asDouble();
		double value2 = args.get(1).asDouble();
		return DoubleParameter.from(Math.atan2(value1, value2));
	}

	private static Parameter sinhFunc(ArrayList<Parameter> args) {
		checkArgumentSize("sinh", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.sinh(value));
	}

	private static Parameter coshFunc(ArrayList<Parameter> args) {
		checkArgumentSize("cosh", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.cosh(value));
	}

	private static Parameter tanhFunc(ArrayList<Parameter> args) {
		checkArgumentSize("tanh", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.tanh(value));
	}

	private static Parameter hypotFunc(ArrayList<Parameter> args) {
		checkArgumentSize("hypot", args,2);
		double value1 = args.get(0).asDouble();
		double value2 = args.get(1).asDouble();
		return DoubleParameter.from(Math.hypot(value1, value2));
	}

	private static Parameter sqrtFunc(ArrayList<Parameter> args) {
		checkArgumentSize("sqrt", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.sqrt(value));
	}

	private static Parameter powFunc(ArrayList<Parameter> args) {
		checkArgumentSize("pow", args,2);
		double value1 = args.get(0).asDouble();
		double value2 = args.get(1).asDouble();
		return DoubleParameter.from(Math.pow(value1, value2));
	}

	private static Parameter expFunc(ArrayList<Parameter> args) {
		checkArgumentSize("exp", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.exp(value));
	}

	private static Parameter logFunc(ArrayList<Parameter> args) {
		checkArgumentSize("log", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.log(value));
	}

	private static Parameter log10Func(ArrayList<Parameter> args) {
		checkArgumentSize("log10", args,1);
		double value = args.get(0).asDouble();
		return DoubleParameter.from(Math.log10(value));
	}

	private static Parameter fmodFunc(ArrayList<Parameter> args) {
		checkArgumentSize("fmod", args,2);
		double value1 = args.get(0).asDouble();
		double value2 = args.get(1).asDouble();
		return DoubleParameter.from(value1 % value2);
	}

}
