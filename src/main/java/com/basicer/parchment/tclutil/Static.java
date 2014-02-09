package com.basicer.parchment.tclutil;



import com.basicer.parchment.*;

import com.basicer.parchment.TargetedCommand;
import com.basicer.parchment.parameters.DelegateParameter;
import com.basicer.parchment.parameters.Parameter;


public class Static extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {

		Parameter t = ctx.getThis();
		Debug.trace("This = " + t);
		if ( t == null ) return EvaluationResult.makeError("Couldn't find spell context");
		DelegateParameter tt = t.cast(DelegateParameter.class);
		if ( tt == null ) return EvaluationResult.makeError("This wasen't a delegate");
		TCLCommand cmd = tt.asTCLCommand(ctx);
		if ( !( cmd instanceof TargetedCommand)) return EvaluationResult.makeError("This wasent a TargetedCommand");

		Context src = ((TargetedCommand) cmd).getSpellContext();
		Debug.trace("This spell " + ((TargetedCommand) cmd).getName() + " ctx has:" + src.getDebuggingString());
		for ( Parameter p : ctx.getArgs() ) {
			ctx.up(1).linkVariableFromContext(src, p.asString());
		}
		
		return EvaluationResult.OK;
	}

}
