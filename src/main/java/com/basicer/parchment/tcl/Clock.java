package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class Clock extends OperationalTCLCommand {

	@Override
	public String getName() { return "clock"; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}



	public Parameter secondsOperation(Parameter dummy, Context ctx) {
		return Parameter.from((int)(System.currentTimeMillis() / 1000));
	}


}
