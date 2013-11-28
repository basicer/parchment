package com.basicer.parchment;

import java.util.*;

import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tclstrings.Documentation;


public abstract class TCLCommand {
	
	public String getName() { return this.getClass().getSimpleName().toLowerCase(); }
	protected TCLCommand getThis() { return null; }
	public String[] getArguments() { return new String[] {"args"}; }
	public String[] getAliases() { return new String[] {}; }

	//public abstract String[] getArguments();



	public Context bindContext(Parameter[] params, Context ctx) {
		class ParamInfo {
			String name;
			boolean required;
			boolean	noise;
		}
		
		String[] args = this.getArguments();
		Context put = ctx.createSubContext();
		boolean and_args = false;
		
		int required = 0;
		int given = params.length - 1;
		
		ArrayList<ParamInfo> xargs = new ArrayList<ParamInfo>();
		ArrayList<String> flags = new ArrayList<String>();
		for ( int i = 0; i < args.length; ++i ) {
			String name = args[i];
			if ( (i+1) == args.length && name.endsWith("args") ) {
				and_args = true;
				break;
			}
			
			ParamInfo nfo = new ParamInfo();
			nfo.required = true;
			nfo.noise = false;
			boolean is_flag = false;
			
			if ( name.endsWith("?") ) {
				nfo.required = false;
				name = name.substring(0, name.length() - 1);
			} 
			
			if ( name.startsWith("-") ) {
				is_flag = true;
				nfo.required = false;
				name = name.substring(1);
			} 

			if ( name.startsWith("'") && name.endsWith("'")) {
				nfo.noise = true;
				name = name.substring(1, name.length() - 1);
			}

			if ( is_flag ) {
				flags.add("-" + name);
			} else {
				nfo.name = name;
				xargs.add(nfo);
			}
			
			if ( nfo.required ) ++required;
		}
		
		
		int ptr = 1;
		for ( int i = 0; i < xargs.size(); ++i ) {
			ParamInfo nfo = xargs.get(i);
			if ( ptr >= params.length ) break;
			if ( (params[ptr] instanceof StringParameter) ) {
				String str  = params[ptr].asString(ctx);

				if ( str.startsWith("-") ) { //TODO: TCL might want us to check the spot
					if ( flags.contains(str + "=")  ) {
						++ptr;
						put.put(str.substring(1), params[ptr]);
						++ptr; --i; continue;
					} else if ( flags.contains(str) ) {
						put.put(str.substring(1), Parameter.from(true));
						++ptr; --i; continue;
					}
				}
			}
			if ( nfo.required || ( given > required ) ) {
				if ( nfo.noise && !params[ptr].asString(ctx).equals(nfo.name) ) {
					continue;
				}
				
				put.put(nfo.name, params[ptr]);
				if ( nfo.required ) --required;
				--given;
				++ptr;
			} 			
		}
		
		if ( required > 0 ) {
			throw new FizzleException(wrongArgumentsString());
		}
		
		for ( int i = 0; i < xargs.size(); ++i ) {
			if ( !put.has(xargs.get(i).name) ) put.put(xargs.get(i).name, null);
		}
		
		if ( and_args ) put.put("args", Parameter.createList(params, ptr, params.length - 1));
		else if ( ptr < params.length ) { throw new FizzleException(wrongArgumentsString()); }
			
		
		
		if ( getThis() != null ) put.setThis(Parameter.from(getThis()));
		return put;
	}

	private String wrongArgumentsString() {
		StringBuilder b = new StringBuilder();
		b.append("wrong # args: should be \"");
		b.append(getName());
		for ( String s : getArguments() ) {
			if (s.equals("args")) {
				b.append(" ?arg ...?");
				continue;
			}
			b.append(" ");
			if ( s.endsWith("?")) b.append("?");
			b.append(s);
		}
		b.append("\"");
		return b.toString();
	}
	
	public String getDescription() { 
		String def = Documentation.getBody(getName());
		if ( def != null ) return "[[http://www.tcl.tk/man/tcl8.6/TclCmd/contents.htm|From the TCL Documentation]]\n\n" + def;
		return "To be written....";
		
	}

	protected String getHelpHeader() {
		StringBuilder b = new StringBuilder();
		b.append("----\n");
		b.append(String.format("=== %s ===\n\n", getName()));
		
		b.append("**" + getName() + "** - ");
		for ( String s : getArguments() ) b.append( "//" + s + "// ");
		return b.toString();
	}
	
	public String getHelpText() {
		StringBuilder b = new StringBuilder();
		b.append(getHelpHeader());
		b.append("\n\n");
		b.append(getDescription());
		b.append("\n\n");
		return b.toString();
	}

	public boolean supportedByServer() { return true; }
		
	public abstract EvaluationResult extendedExecute(Context c2, TCLEngine e);
}
