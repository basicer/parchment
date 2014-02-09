package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.EvaluationResult.EvalCallback;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;

public class Catch extends TCLCommand {
	@Override
	public String[] getArguments() { return new String[] { "script", "messageVarName?", "optionsVarName?"  }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, TCLEngine e) {
		final Parameter code = ctx.get("script");

		
		return new BranchEvaluationResult(code.asString(), ctx.up(1), new EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
				if ( ctx.get("messageVarName") != null ) {
					Set.access( ctx.get("messageVarName").asString(), true, last.getValue(), ctx.up(1));
				}

				if ( ctx.get("optionsVarName") != null ) {
					DictionaryParameter opts = new DictionaryParameter();
					opts.writeIndex("-code", IntegerParameter.from(last.getCode().ordinal()));
					Set.access( ctx.get("optionsVarName").asString(), true, opts, ctx.up(1));
				}
				return EvaluationResult.makeOkay(Parameter.from(last.getCode().ordinal()));
			}
			
			
		});
		
		//
	}
}
