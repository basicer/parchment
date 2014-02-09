package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.VectorParameter;


/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 9/9/13
 * Time: 2:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class Dist extends TCLCommand {


	@Override
	public String[] getArguments() { return new String[] { "a", "b" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		return EvaluationResult.makeOkay(execute(ctx));
	}

	public Parameter execute(Context ctx) {
		VectorParameter a = ctx.get("a").cast(VectorParameter.class);
		VectorParameter b = ctx.get("b").cast(VectorParameter.class);
		return Parameter.from(a.asVector().distance(b.asVector()));
	}
}
