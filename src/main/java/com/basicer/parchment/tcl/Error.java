package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Error extends TCLCommand {


	@Override
	public String[] getArguments() { return new String[] { "info?" }; }

	@Override
	public EvaluationResult extendedExecute(Context c2, TCLEngine e) {
		return new EvaluationResult(c2.get("info"), EvaluationResult.Code.ERROR);
	}
	
	

}