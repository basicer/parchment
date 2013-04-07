package com.basicer.parchment.tcl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Queue;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public abstract class OperationalTCLCommand extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }
	
	public static <U,TT extends Parameter> Parameter operationalDispatch(TCLCommand command, Class<U> type, TT target, Context ctx, Queue<Parameter> args) {
	
		if ( target == null ) throw new FizzleException("No target.");
		Object o = target.getUnderlyingValue();
		
		if ( !type.isInstance(o) ) throw new FizzleException("Target mismatch");
	
		U obj = (U) o;
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
			} else if ( op.equals("new") ) {
				try {
					Method m = c.getMethod("create", Context.class);
					U ni = (U) m.invoke(command, ctx);
					obj = ni;
					
					out = Parameter.from(obj);
					target = (TT)Parameter.from(ni);
					continue;
				} catch (NoSuchMethodException e) {
					throw new FizzleException("No such operation: " + op);
				} catch (SecurityException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}

	
			
			
			out = invokeMapped(command, op, args, ctx, obj);
			if ( out != null ) {
				Object ov = out.getUnderlyingValue();
				if ( type.isInstance(ov) ) {
					Debug.trace("Obj now " + ov);
					obj = (U)ov;
				}
			}
			
		}
		
		
		return out;
		
	}

	public static Parameter invokeMapped(TCLCommand command, String op, Queue<Parameter> args, Context ctx, Object obj  ) {
		Class<?> c = command.getClass();
		//Method m = locateMethod(c, op);
		//TODO: This only goes one level deep
		//if ( command instanceof OperationalSpell<?> ) {
		//	OperationalSpell<?> os = (OperationalSpell<?>) command;
		//	if ( m == null && os.getBaseClass() != null ) m = locateMethod(os.getBaseClass(), op);
		//}
		
		Class<?> tc = c;
		Method m = locateMethod(tc, op);
		if ( command instanceof OperationalSpell<?> ) {
			while ( m == null ) {
				try {
					Method up = tc.getMethod("getBaseClass");
					tc = (Class<?>)up.invoke(null);
					if ( tc == null ) break;
					m = locateMethod(tc, op);
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
		
		Class[] method_types = m.getParameterTypes();
		Object[] method_args = new Object[method_types.length];
		method_args[0] = obj;
		method_args[1] = ctx;
		for ( int i = 2; i < method_args.length; ++i ) {
			if ( args.size() < 1 ) break;
			Parameter p = args.peek();
			if ( p.asString() != null && p.asString().startsWith("-") ) break;
			
			args.poll();
			if ( method_types[i].getSimpleName().equals("List") ) { //TODO: Surely we can do better.
				ArrayList<Parameter> array = new ArrayList<Parameter>();
				array.add(p);
				while ( args.size() > 0 ) array.add(args.poll());
				method_args[i] = array;
			} else if ( method_types[i].equals(Parameter.class)) {
				method_args[i] = p;
			} else {
				try { 
					method_args[i] = p.cast(method_types[i], ctx);
				} catch ( Exception ex ) { }
				if ( method_args[i] == null ) throw new FizzleException(op + " expected " + method_types[i].getSimpleName() + ", got " + p.getClass().getSimpleName());
			}
		}
		try {
			return (Parameter)m.invoke(command, method_args);
			

		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			if ( e.getTargetException() instanceof RuntimeException ) { 
				throw (RuntimeException)e.getTargetException();
			} else {
				throw new RuntimeException(e.getTargetException());
			}
		}
	}
	
	public static Method locateMethod(Class<?> c, String op) {
		Method[] methods = c.getMethods();
		for ( Method mc : methods ) {
			if ( !mc.getName().equals(op + "Operation") ) continue;
			return mc;
		}
		
		return null;
	}


}
