package com.basicer.parchment.tcl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.*;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.Parameter;



public abstract class OperationalTCLCommand extends TCLCommand {

	protected class WrongNumberOfArgumentsException extends IllegalArgumentException {

	}

	@Override
	public String[] getArguments() { return new String[] { "args" }; }


	public EvaluationResult basicExtendedExecute(Context ctx, TCLEngine e) {
		try {

			Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());

			Parameter operation = args.poll();
			String op = operation.asString();

			if ( op == null ) throw new FizzleException("Operation not a string.");
			if ( op.startsWith("-") ) op = op.substring(1, op.length());

			EvaluationResult out = invokeMapped(this, op, args, ctx, null);
			return out;

		} catch ( FizzleException ex ) {
			return EvaluationResult.makeError(ex.getMessage());
		}
	}


	public static <U,TT extends Parameter> Parameter operationalDispatch(TCLCommand command, Class<U> type, TT target, Context ctx, Queue<Parameter> args) {


		Object o = ( target == null ) ? null : target.getUnderlyingValue();
		
		if ( o != null && !type.isInstance(o) ) throw new FizzleException("Target mismatch");
	
		if ( args.size() < 1 ) return target;
	
		
		
		Class<?> c = command.getClass();
		Parameter out = null;
		while ( args.size() > 0 ) {
			Parameter operation = args.poll();
			String op = operation.asString();
			
			if ( op == null ) throw new FizzleException("Operation not a string.");
			if ( op.startsWith("-") ) op = op.substring(1, op.length());
			if ( op.equals("self") ) {
				out = target;
				continue;
			} else if ( op.equals("new") || op.equals("create") ) {
				try {
					Method m = locateOperation(c, "create", "");
					//TODO: We make this not strict for backward compatability, but we might want
					//to drop it before it's too late.
					Object[] method_args = prepareMethodCall(op, args, ctx, null, m, false);
					U ni = null;
					try {
						ni = (U) m.invoke(command, method_args);
					} catch ( InvocationTargetException ex ) {
						if ( ex.getTargetException() instanceof  FizzleException ) throw (FizzleException)ex.getTargetException();
						throw ex;
					}


					target = (TT)Parameter.from(ni);
					out = target;
					continue;
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e.getTargetException());
				}
			}

			if ( target == null ) throw new FizzleException("No target.");
			out = invokeMapped(command, op, args, ctx, target).getValue();
			if ( out != null ) {
				Object ov = out.getUnderlyingValue();
				if ( ov != null && type.isInstance(ov) ) {
					Debug.trace("Obj now " + ov);
					target = (TT) Parameter.from(ov);
				}
			}
			
		}
		
		
		return out;
		
	}

	public static EvaluationResult invokeMapped(TCLCommand command, String op, Queue<Parameter> args, Context ctx, Parameter obj  ) {
		Class<?> c = command.getClass();
		//Method m = locateOperation(c, op);
		//TODO: This only goes one level deep
		//if ( command instanceof OperationalSpell<?> ) {
		//	OperationalSpell<?> os = (OperationalSpell<?>) command;
		//	if ( m == null && os.getBaseClass() != null ) m = locateOperation(os.getBaseClass(), op);
		//}
		
		Class<?> tc = c;
		Method m = locateOperation(tc, op, "Operation");
		if ( command instanceof OperationalSpell<?> ) {
			while ( m == null ) {
				try {
					Method up = tc.getMethod("getBaseClass");
					tc = (Class<?>)up.invoke(null);
					if ( tc == null ) break;
					m = locateOperation(tc, op, "Operation");
				} catch (NoSuchMethodException e) {
					break;
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		if ( m == null ) throw new FizzleException("No such operation on " + command.getName() + ": " + op);
		
		Object[] method_args = prepareMethodCall(op, args, ctx, obj, m, true);
		try {
			Object o = m.invoke(command, method_args);
			if ( o == null ) return EvaluationResult.OK;
			else if ( o instanceof Parameter ) return new EvaluationResult((Parameter) o, EvaluationResult.Code.OK);
			else if ( o instanceof EvaluationResult ) return (EvaluationResult) o;
			else return null;
			

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {

			if ( e.getTargetException() instanceof WrongNumberOfArgumentsException ) {
				Operation oa = m.getAnnotation(Operation.class);
				if ( oa == null || oa.argnames() == null) throw new FizzleException("wrong # args: args unknown");
				StringBuilder sb = new StringBuilder();
				for ( String s : oa.argnames() ) {
					if (sb.length() > 0 ) sb.append(" ");
					if ( s.endsWith("?") ) sb.append("?");
					sb.append(s);
				}
				throw new FizzleException("wrong # args: should be \"command " + op + " " + sb.toString() + "\"");
			}

			if ( e.getTargetException() instanceof RuntimeException ) { 
				throw (RuntimeException)e.getTargetException();
			} else {
				throw new RuntimeException(e.getTargetException());
			}
		}
	}

	private static Object[] prepareMethodCall(String op, Queue<Parameter> args, Context ctx, Parameter obj, Method m, boolean strict) {
		Class[] method_types = m.getParameterTypes();
		Object[] method_args = new Object[method_types.length];
		int i = 0;
		if ( method_types[0] != Context.class) {
			method_args[i++] = obj == null ? null : obj.getUnderlyingValue();
		}
		method_args[i++] = ctx;
		for (; i < method_args.length; ++i ) {
			if ( args.size() < 1 ) break;
			Parameter p = args.peek();
			//if ( p.asString() != null && p.asString().startsWith("-") ) break;
			
			if ( method_types[i].getSimpleName().equals("List") ) { //TODO: Surely we can do better.
				args.poll();
				ArrayList<Parameter> array = new ArrayList<Parameter>();
				array.add(p);
				while ( args.size() > 0 ) array.add(args.poll());
				method_args[i] = array;
			} else if ( method_types[i].equals(Parameter.class)) {
				args.poll();
				method_args[i] = p;
			} else {
				try { 
					method_args[i] = p.cast(method_types[i], ctx);
				} catch ( Exception ex ) { }
				if ( method_args[i] == null ) {
					Debug.info("Failed cast: " + method_types[i].getSimpleName());
					if ( strict ) throw new FizzleException(op + " expected " + method_types[i].getSimpleName() + ", got " + p.getClass().getSimpleName());
					else return method_args;
				} else {
					args.poll();
				}
			}
		}

		return method_args;
	}

	public static Method locateOperation(Class<?> c, String op, String suffix) {
		Method[] methods = c.getMethods();
		//TODO: This whole business needs to be cached.
		for ( Method mc : methods ) {
			
			if ( !mc.getName().equalsIgnoreCase(op + suffix) ) {

				Operation opp = mc.getAnnotation(Operation.class);
				if ( opp == null ) continue;
				if ( opp.aliases() == null ) continue;
				for ( String s : opp.aliases() ) {
					if ( s.equals(op) ) return mc;
				}
				continue;
			
			}
			return mc;
		}
		
		return null;
	}


}
