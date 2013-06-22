package com.basicer.parchment.tclutil;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.tcl.Expr;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 6/18/13
 * Time: 11:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class With extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "-each", "newtarget", "code" }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, TCLEngine e) {


		final Queue<Parameter> targets = new LinkedList<Parameter>();
		if ( ctx.get("each") != null ) {
			for ( Parameter p : ctx.get("newtarget") ) {
				targets.add(p);
			}
		} else {
			targets.add(ctx.get("newtarget"));
		}
		return new EvaluationResult.BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {

			public EvaluationResult result(EvaluationResult er) {
				final EvaluationResult.EvalCallback again = this;
				if ( targets.size() < 1 ) return EvaluationResult.OK;
				ctx.setTarget(targets.poll());
				Debug.info("Target now" + ctx.getTarget().toString());
				return new EvaluationResult.BranchEvaluationResult(ctx.get("code").asString(), ctx, again);
				}

			}
		);

	}
}
