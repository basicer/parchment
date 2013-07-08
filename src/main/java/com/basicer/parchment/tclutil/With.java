package com.basicer.parchment.tclutil;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.ListParameter;
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
	public String[] getArguments() { return new String[] { "-each", "-merge", "newtarget", "code" }; }

	@Override
	public EvaluationResult extendedExecute(final Context ctx, TCLEngine e) {


		final Queue<Parameter> targets = new LinkedList<Parameter>();
		boolean each = false;
		if ( ctx.get("each") != null ) {
			each = true;
			for ( Parameter p : ctx.get("newtarget") ) {
				targets.add(p);
			}
		} else {
			targets.add(ctx.get("newtarget"));
		}
		final boolean each_c = each;
		final boolean merge_c = ctx.get("merge") != null;
		final LinkedList<Parameter> results = new LinkedList<Parameter>();
		return new EvaluationResult.BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {

			public EvaluationResult result(EvaluationResult er) {
				if ( !(er instanceof EvaluationResult.BranchEvaluationResult) ) {

					if ( er.getValue() != null && er.getCode() == EvaluationResult.Code.RETURN ) {
						boolean doit = true;
						if ( merge_c ) {
							for ( Parameter p : results ) {
								if ( p.valueEquals(er.getValue()) ) doit = false;
							}
						}
						if ( doit ) results.add(er.getValue());
					}
					if ( targets.size() < 1 || er.getCode() == EvaluationResult.Code.BREAK ) {
						if ( each_c ) return new EvaluationResult(ListParameter.from(results), EvaluationResult.Code.OK);
						else return er;
					}
				}
				final EvaluationResult.EvalCallback again = this;
				ctx.setTarget(targets.poll());
				Debug.info("Target now" + ctx.getTarget().toString());
				return new EvaluationResult.BranchEvaluationResult(ctx.get("code").asString(), ctx, again);
				}

			}
		);

	}
}
