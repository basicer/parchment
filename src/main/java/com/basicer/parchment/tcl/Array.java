package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.*;

import java.util.ArrayList;

/**
 * Created by basicer on 1/9/14.
 */
public class Array extends OperationalTCLCommand {
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}

	private DictionaryParameter resolve(StringParameter str, Context ctx) {
		Parameter target = Set.access(str.asString(ctx), false, null, ctx.up(1));
		if ( !(target instanceof DictionaryParameter) ) throw new FizzleException("array on non-array");
		return (DictionaryParameter) target;

	}

	public Parameter sizeOperation(Parameter dummy, Context ctx, StringParameter str) {
		return Parameter.from(resolve(str, ctx).size());
	}

	public Parameter getOperation(Parameter dummy, Context ctx, StringParameter str, StringParameter idx) {
		return Parameter.from(resolve(str, ctx).index(idx.asString()));
	}

	public Parameter namesOperation(Parameter dummy, Context ctx, StringParameter str, StringParameter pattern) {
		DictionaryParameter array = resolve(str, ctx);
		ArrayList<Parameter> out = new ArrayList<Parameter>();
		for ( String s : array.getGetSet() ) {
			if ( pattern == null || StringCmd.GlobMatch(s, pattern.asString()) ) out.add(StringParameter.from(s));
		}

		return ListParameter.from(out);
	}

}
