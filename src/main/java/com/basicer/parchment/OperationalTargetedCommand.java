package com.basicer.parchment;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.tcl.OperationalTCLCommand;

public class OperationalTargetedCommand<T extends Parameter> extends TargetedCommand {

	public static Class<? extends OperationalTargetedCommand<?>> getBaseClass() { return null; }

	@Override
	public String[] getArguments() { return new String[] { "args" }; }

	protected Parameter doaffect(T target, Context ctx) {
		Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());

		if ( args.size() > 0 ) {

		}
		return OperationalTCLCommand.operationalDispatch(this, target == null ? null : target.getUnderlyingType(), target, ctx, args);
	}

	protected Parameter doaffect(T target, Context ctx, Class<?> type) {
		Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());

		if ( args.size() > 0 ) {

		}
		return OperationalTCLCommand.operationalDispatch(this, type, target, ctx, args);
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
				if ( this.getFirstParameterTargetType(null) != FirstParameterTargetType.Never ) b.append("target? ");
				b.append("**" + name + "**");
				Class[] ptypes = m.getParameterTypes();
				Operation annotation = m.getAnnotation(Operation.class);
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
				if ( annotation != null ) {
					String text = annotation.desc();
					if ( text.length() > 0 ) {
						b.append("\n   \\\\");
						b.append(text);
						b.append("\n");
					}
				} else {
					b.append("\n   \\\\ Undocumented\n");
				}
				b.append("\n\n");
			}
		}


		return b.toString();
	}

	@Override
	public java.util.List<String> tabComplete(String[] args) {
		LinkedList<String> argsq = new LinkedList<String>(Arrays.asList(args));
		//TODO: Check if first argument will be eaten as target
		return OperationalTCLCommand.tabComplete(this.getClass(), argsq);
	}

}
