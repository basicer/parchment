package com.basicer.parchment;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

import com.avaje.ebean.enhance.asm.Type;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.tcl.OperationalTCLCommand;

public class OperationalSpell<T extends Parameter> extends Spell {
	
	public static Class<? extends OperationalSpell<?>> getBaseClass() { return null; }
	
	@Override
	public String[] getArguments() { return new String[] { "args" }; }

	protected Parameter doaffect(T target, Context ctx) {
		Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());
	
		if ( args.size() > 0 ) {
			
		}
		return OperationalTCLCommand.operationalDispatch(this, target.getUnderlyingType(), target, ctx, args);
	}
	

	
	public String getHelpText() {
		StringBuilder b = new StringBuilder();
		b.append(getHelpHeader());
		b.append("\n\n");
		
		b.append(getDescription());
		b.append("\n\n");
		
		for ( Method m : getClass().getDeclaredMethods() ) {
			if ( m.getName().endsWith("Operation") || m.getName().equals("create") ) {
				String name = m.getName();
				if ( name.endsWith("Operation") ) name = name.substring(0, m.getName().length() - 9 );
				
				
				b.append("\n");
				b.append("* **" + getName() + "** ");
				if ( this.getFirstParamaterTargetType(null) != FirstParamaterTargetType.Never ) b.append("target? ");
				b.append("**" + name + "**");
				Class[] ptypes = m.getParameterTypes();
				for ( int i = 1; i < ptypes.length; ++i ) {
					if ( ptypes[i] == Context.class ) continue;
					String tname = ptypes[i].getSimpleName();
					if ( tname.endsWith("Parameter") && !tname.equals("Parameter") ) {
						tname = tname.substring(0, tname.indexOf("Parameter")).toLowerCase();
						b.append(" //" + tname + (i-1) + "//");
					} else {
						b.append(" //arg" + (i-1) + "//");
					}
				}
				b.append("\n\n");
			}
		}
		

		return b.toString();
	}
	
}
