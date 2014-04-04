package com.basicer.parchment.extra;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;
import com.google.gson.JsonSyntaxException;


import java.io.StringReader;

/**
 * Created by basicer on 2/16/14.
 */
public class Nbt extends TCLCommand {
	@Override
	public String[] getArguments() { return new String[] {"object"}; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter object = ctx.get("object");
		Object o = object.getUnderlyingValue();
		Object handle = Reflect.getFieldValue(o, Object.class, "handle");
		if ( handle == null ) return EvaluationResult.makeError("Couldn't get handle to NMS object");
		Object otag = Reflect.invokeMethod(handle, Object.class, "getTag");

		if ( otag == null ) return EvaluationResult.makeError("Couldn't get tag from to NMS object");

		try {
			return EvaluationResult.makeOkay(Json.JSONToTCL(otag.toString()));
		} catch (JsonSyntaxException ex ) {
			throw new FizzleException("Invalid Json: " + ex.getMessage());
		}




	}
}
