package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;

public class Unset extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "-nocomplain", "args" }; }
	
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		Context ctxu = ctx.up(1);
		boolean nocomplain = ctx.has("nocomplain");
		for ( Parameter p : ctx.getArgs() )
			if ( nocomplain ) {
				try { Set.access(p.asString(), true,  null, ctxu); }
				catch ( Exception ex ) { }
			} else {
				try {
					Set.access(p.asString(), false, null, ctxu); //Make sure variable exists;
				} catch ( FizzleException ex ) {
					throw new FizzleException(ex.getMessage().replaceFirst("read","unset"));
				}
				Set.access(p.asString(), true,  null, ctxu);
			}
		
		return EvaluationResult.OK;
	}

}
