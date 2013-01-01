package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class Return extends TCLCommand {

	@Override
	public Parameter execute(Context ctx) { throw new RuntimeException("Wrong break Invocation"); }

	@Override
	public String[] getArguments() { return new String[] { "result?" }; }

	@Override
	public EvaluationResult extendedExecute(Context c2) {
		return new EvaluationResult(c2.get("result"),  EvaluationResult.Code.RETURN);
	}
	
	

}
