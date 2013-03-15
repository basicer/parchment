package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;

public class Proc extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "name", "argNames", "body" };
	}

	
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter pname = ctx.get("name");
		Parameter pargs = ctx.get("argNames");
		Parameter pbody = ctx.get("body");
		
		String[] xargs = null;
		if ( pargs.asString().length() > 0 ) { 
			xargs = pargs.asString().split(" ");
		} else {
			xargs = new String[0];
		}
		
		
		final String bodystr = pbody.asString();
		final String[] cxargs = xargs;
		
		TCLCommand thiz = null;
		Parameter thizp = ctx.getThis();
		if ( thizp != null ) thiz = thizp.as(TCLCommand.class, ctx);
		
		final TCLCommand thizc = thiz;
		
		TCLCommand proc = new TCLCommand() {

			@Override
			public String[] getArguments() { return cxargs; }
			
			@Override
			public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
				if ( engine == null ) engine = new TCLEngine("", null);
				EvaluationResult r = engine.evaluate(bodystr, ctx);
				if ( r == null ) return EvaluationResult.OK;
				if ( r.getCode() == Code.RETURN ) {
					r.setCode(Code.OK);
				}
				return r;
			}
			
			@Override
			public TCLCommand getThis() { return thizc; }
			
		};
		ctx.top().setCommand(pname.asString(), proc);
		Debug.trace("Registered proc " + pname.asString());
		return new EvaluationResult(Parameter.from(proc));
		
	}

}
