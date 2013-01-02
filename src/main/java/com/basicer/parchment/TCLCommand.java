package com.basicer.parchment;

import java.io.IOException;
import java.io.StringReader;
import java.security.Policy.Parameters;
import java.util.*;

import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;


public abstract class TCLCommand {
	
	public abstract Parameter execute(Context ctx);
	
	public String getName() { return this.getClass().getSimpleName(); }
	
	public String[] getArguments() { return new String[] {"args"}; }
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
				name = name.substring(1,  name.length());
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
				if ( str.startsWith("-") && flags.contains(str) ) { 
					ctx.put(str.substring(1, str.length()), Parameter.from(true));
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
			throw new RuntimeException("Command " + getName() + " required " + required + " more arguements");
		}
		
		if ( and_args ) put.put("args", Parameter.createList(params, ptr, params.length - 1));
		
		return put;
	}

	public EvaluationResult extendedExecute(Context c2) {
		return new EvaluationResult(this.execute(c2));
	}
}
