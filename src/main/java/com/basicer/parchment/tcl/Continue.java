package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;

public class Continue extends TCLCommand {


	@Override
	public String[] getArguments() { return new String[] { }; }

	@Override
	public EvaluationResult extendedExecute(Context c2, TCLEngine e) {
		return new EvaluationResult(null, Code.CONTINUE);
	}
	
	

}