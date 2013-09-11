package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;
import org.bukkit.Location;

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
		return new EvaluationResult(execute(ctx));
	}

	public Parameter execute(Context ctx) {
		Location a = ctx.get("a").as(Location.class);
		Location b = ctx.get("b").as(Location.class);
		return Parameter.from(a.distance(b));
	}
}
