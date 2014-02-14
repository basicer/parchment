package com.basicer.parchment.test;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;

import java.util.*;

public class TestConstraint extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "name", "test" };
	}


	public static boolean getConstraint(String name) {
		if ( constraints == null ) return false;
		if ( !constraints.containsKey(name) ) return false;
		return constraints.get(name);
	}

	public static HashMap<String, Boolean> constraints;

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {



		String name = ctx.get("name").asString();
		if ( constraints == null ) constraints = new HashMap<String, Boolean>();


		constraints.put(name, ctx.get("test").asBoolean());
		Debug.info("Cosntraint added: " + name + " " + ctx.get("test").asBoolean() );

		return EvaluationResult.OK;
	}

	public static void setConstraint(String constraint, boolean value) {
		if ( constraints == null ) constraints = new HashMap<String, Boolean>();
		constraints.put(constraint, value);
	}
}
