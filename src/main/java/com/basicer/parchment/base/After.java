package com.basicer.parchment.base;

import java.util.concurrent.Callable;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.ThreadManager;
import com.basicer.parchment.parameters.DelegateParameter;
import com.basicer.parchment.parameters.Parameter;

public class After extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "delay", "code?" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		
		Parameter delay = ctx.get("delay");
		Parameter code = ctx.get("code");
		
		Double d = delay.asDouble();
		final long when = (long)(System.currentTimeMillis() + d * 1000);
		
		Callable<Long> whenfunc = new Callable<Long>() {
			public Long call() throws Exception {
				return when;
			}
		};
		
		if ( d == null ) return EvaluationResult.makeError("delay should be a number");
		if ( code == null ) {
			
			return new EvaluationResult.BranchEvaluationResult(null, ctx, null, whenfunc);
		}
		ThreadManager.instance().submitWork(new EvaluationResult.BranchEvaluationResult(code.asString(ctx), ctx.up(1).mergeAndCopyAsGlobal(), null, whenfunc));
		return EvaluationResult.OK;
	}
	
}
