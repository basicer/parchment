package com.basicer.parchment;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import com.basicer.parchment.parameters.Parameter;

public abstract class TCLCommand {
	

	public abstract Parameter execute(Context ctx);
	
	public String[] getArguments() { return new String[] {}; }
	
	public Context bindContext(Parameter[] params, Context ctx) {
		String[] args = this.getArguments();
		Context put = ctx.createSubContext();
		for ( int i = 0; i < args.length; ++i ) {
			if ( (i+1) >= params.length ) break;
			if ( (i+1) == args.length && args[i] == "args" ) {
				put.put("args", Parameter.createList(params, i+1, params.length -1));
				break;
			}
			put.put(args[i], params[i+1]);
		}
		
		return put;
	}
}
