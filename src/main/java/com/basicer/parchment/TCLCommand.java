package com.basicer.parchment;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import com.basicer.parchment.parameters.Parameter;


public abstract class TCLCommand {
	
	public abstract Parameter execute(Context ctx);
	
	public String[] getArguments() { return new String[] {"args"}; }
	//public abstract String[] getArguments();
	
	public Context bindContext(Parameter[] params, Context ctx) {
		class ParamInfo {
			String name;
			boolean required;
			boolean	noise;
		}
		
		for ( int i = 0; i < params.length; ++i ) {
			System.out.println("-> " + i + " " + params[i].asString());
		}
		
		String[] args = this.getArguments();
		Context put = ctx.createSubContext();
		boolean and_args = false;
		
		int required = 0;
		int given = params.length - 1;
		
		ArrayList<ParamInfo> xargs = new ArrayList<ParamInfo>();
		for ( int i = 0; i < args.length; ++i ) {
			String name = args[i];
			if ( (i+1) == args.length && name.endsWith("args") ) {
				and_args = true;
				break;
			}
			
			ParamInfo nfo = new ParamInfo();
			nfo.required = true;
			nfo.noise = false;
			
			
			if ( name.endsWith("?") ) {
				nfo.required = false;
				name = name.substring(0, name.length() - 1);
			} 

			if ( name.startsWith("'") && name.endsWith("'")) {
				nfo.noise = true;
				name = name.substring(1, name.length() - 1);
			}

			
			nfo.name = name;
			xargs.add(nfo);
			
			if ( nfo.required ) ++required;
		}
		
		System.out.println("Given: " + given + ", required: " + required);
		int ptr = 1;
		for ( int i = 0; i < xargs.size(); ++i ) {
			ParamInfo nfo = xargs.get(i);
			if ( ptr >= params.length ) break;
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
			throw new RuntimeException("Command required " + required + " more arguements");
		}
		
		if ( and_args ) put.put("args", Parameter.createList(params, ptr, params.length - 1));
		
		return put;
	}
}
