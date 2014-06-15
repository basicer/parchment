package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class Proc extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "name", "argNames", "body" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter pname = ctx.get("name");
		Parameter pargs = ctx.get("argNames");
		final StringParameter pbody = ctx.get("body").castToStringParameter();

		String[] xargs = null;
		if (pargs.asString().length() > 0) {
			xargs = pargs.asString().split(" ");
		} else {
			xargs = new String[0];
		}


		final String[] cxargs = xargs;

		TCLCommand thiz = null;
		Parameter thizp = ctx.getThis();
		if (thizp != null)
			thiz = thizp.as(TCLCommand.class, ctx);

		final TCLCommand thizc = thiz;

		TCLCommand proc = new TCLCommand() {

			@Override
			public String[] getArguments() {
				return cxargs;
			}

			@Override
			public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
				return new BranchEvaluationResult(pbody, ctx, new EvaluationResult.EvalCallback() {
					public EvaluationResult result(EvaluationResult r) {
						if (r == null)
							return EvaluationResult.OK;
						if (r.getCode() == Code.RETURN) {
							r.setCode(Code.OK);
						}
						return r;

					}
				});
			}

			@Override
			public TCLCommand getThis() {
				return thizc;
			}

		};
		ctx.top().setCommand(pname.asString(), proc);
		Debug.trace("Registered proc " + pname.asString());
		// return new EvaluationResult(Parameter.from(proc));
		return EvaluationResult.OK;

	}

}
