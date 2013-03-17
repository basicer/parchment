package com.basicer.parchment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

import com.avaje.ebean.enhance.asm.Type;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.Parameter;

public class OperationalSpell<T extends Parameter> extends Spell {
	
	public Class<? extends OperationalSpell<?>> getBaseClass() { return null; }
	
	@Override
	public String[] getArguments() { return new String[] { "args" }; }

	protected Parameter doaffect(T target, Context ctx) {
		Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());
		
		
		
		if ( args.size() > 0 ) {
			
		}
		return dispatch(target.getUnderlyingType(), target, ctx, args);
	}
	
	private <U> Parameter dispatch(Class<U> type, T target, Context ctx, Queue<Parameter> args) {

		if ( target == null ) fizzle("No target.");
		Object o = target.getUnderlyingValue();
		
		if ( !type.isInstance(o) ) fizzle("Target mismatch");

		U obj = (U) o;
		if ( args.size() < 1 ) return target;

		Class<?> c = this.getClass();
		
		
		Parameter out = null;
		while ( args.size() > 0 ) {
			Parameter operation = args.poll();
			String op = operation.asString();
			
			if ( op == null ) fizzle("Operation not a string.");
			if ( op.startsWith("-") ) op = op.substring(1, op.length());
			if ( op.equals("self") ) {
				out = target;
				continue;
			} else if ( op.equals("new") ) {
				try {
					Method m = this.getClass().getMethod("create", Context.class);
					U ni = (U) m.invoke(this, ctx);
					obj = ni;
					out = Parameter.from(obj);
					continue;
				} catch (NoSuchMethodException e) {
					fizzle("No such operation: " + op);
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
			Method m = locateMethod(c, op);
			//TODO: This only goes one level deep
			if ( m == null && getBaseClass() != null ) m = locateMethod(getBaseClass(), op);

			
			if ( m == null ) fizzle("No such operation on " + this.getName() + ": " + op);
			Class[] method_types = m.getParameterTypes();
			Object[] method_args = new Object[method_types.length];
			method_args[0] = obj;
			method_args[1] = ctx;
			for ( int i = 2; i < method_args.length; ++i ) {
				if ( args.size() < 1 ) break;
				Parameter p = args.peek();
				if ( p.asString() != null && p.asString().startsWith("-") ) break;
				
				args.poll();
				if ( method_types[i].equals(Parameter.class)) {
					method_args[i] = p;
				} else {
					method_args[i] = p.cast(method_types[i], ctx);
					if ( method_args[i] == null ) fizzle(op + " expected " + method_types[i].getSimpleName() + ", got " + p.getClass().getSimpleName());
				}
			}
			try {
				out = (Parameter)m.invoke(this, method_args);
				Object ov = out.getUnderlyingValue();
				if ( type.isInstance(ov) ) {
					Debug.trace("Obj now " + ov);
					obj = (U)ov;
				}
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
		
		
		return out;
		
	}
	
	private static Method locateMethod(Class<?> c, String op) {
		Method[] methods = c.getMethods();
		for ( Method mc : methods ) {
			if ( !mc.getName().equals(op + "Operation") ) continue;
			return mc;
		}
		
		return null;
	}
	
	public String getHelpText() {
		StringBuilder b = new StringBuilder();
		b.append(getHelpHeader());
		b.append("\n\n");
		
		b.append(getDescription());
		b.append("\n\n");
		
		for ( Method m : getClass().getDeclaredMethods() ) {
			if ( m.getName().endsWith("Operation") ) {
				String name = m.getName().substring(0, m.getName().length() - 9 );
				
				
				b.append("\n");
				b.append("* " + getName() + " ");
				if ( this.getFirstParamaterTargetType(null) != FirstParamaterTargetType.Never ) b.append("target? ");
				b.append(name);
				Class[] ptypes = m.getParameterTypes();
				for ( int i = 2; i < ptypes.length; ++i ) {
					b.append(" arg" + (i-2));
				}
				b.append("\n\n");
			}
		}
		

		return b.toString();
	}
	
}
