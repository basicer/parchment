package com.basicer.parchment.tcl;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class Dict extends OperationalTCLCommand {

	@Override
	public String getName() { return "dict"; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}



	public Parameter sizeOperation(Parameter dummy, Context ctx, DictionaryParameter dict) {
		return IntegerParameter.from(dict.size());
	}

	public Parameter getOperation(Parameter dummy, Context ctx, DictionaryParameter dict, java.util.List<Parameter> args) {
		Parameter result = dict;
		for ( Parameter p : args ) {
			result = dict.index(p.asString(ctx));
		}
		return result;
	}

	public Parameter createOperation(Parameter dummy, Context ctx, java.util.List<Parameter> args) {
		DictionaryParameter dict = new DictionaryParameter();
		Parameter key = null;
		for ( Parameter p : args ) {
			if ( key == null ) {
				key = p;
				continue;
			}
			dict.writeIndex(key.asString(ctx), p);
			key = null;
		}
		if ( key != null ) throw new FizzleException("Unmatched key/value pair");
		return dict;
	}

}
