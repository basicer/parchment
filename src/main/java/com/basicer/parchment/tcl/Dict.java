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


	private DictionaryParameter findDictOrFizzle(Context ctx, String s) {
		Parameter param = ctx.up(1).get(s);
		if ( param == null ) throw new FizzleException("no such variable: " + s);
		DictionaryParameter casted = param.cast(DictionaryParameter.class);
		if ( casted == null ) throw  new FizzleException("variable " + s + " couldn't be casted to a dictionary");
		return casted;
	}


	public Parameter sizeOperation(Parameter dummy, Context ctx, StringParameter dictname) {
		DictionaryParameter dict = findDictOrFizzle(ctx, dictname.asString());
		return IntegerParameter.from(dict.size());
	}

	public Parameter getOperation(Parameter dummy, Context ctx, StringParameter dictname, java.util.List<Parameter> args) {
		DictionaryParameter dict = findDictOrFizzle(ctx, dictname.asString());
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
