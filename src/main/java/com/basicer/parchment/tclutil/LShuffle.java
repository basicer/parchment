package com.basicer.parchment.tclutil;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 6/21/13
 * Time: 9:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class LShuffle extends TCLCommand {
	@Override
	public String[] getArguments() {
		return new String[] { "list" };
	}

	@Override
	public String getDescription() {
		return "Create a new list by randomizing the order of the items the given list.";
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter list = ctx.get("list").cast(ListParameter.class);
		if ( list == null ) return EvaluationResult.makeOkay(ctx.get("list"));
		LinkedList<Parameter> canidates = new LinkedList<Parameter>();
		for ( Parameter p : list ) {
			canidates.add(p);
		}

		ArrayList<Parameter> out = new ArrayList<Parameter>();
		while ( canidates.size() > 0 ) {
			int idx = (int)(Math.random() * canidates.size());
			out.add(canidates.remove(idx));
		}

		return EvaluationResult.makeOkay(ListParameter.from(out));
	}
}
