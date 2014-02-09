package com.basicer.parchment.tcl;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 7/12/13
 * Time: 11:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class LSearch extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] {"-all", "-inline", "list", "pattern"}; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		boolean all = ctx.get("all") != null;
		boolean inline = ctx.get("inline") != null;

		Parameter l = ctx.get("list");
		if ( l == null ) throw new FizzleException( "No list specified.");

		Parameter search = ctx.get("pattern");
		if ( search == null ) throw new FizzleException("No pattern specified.");

		ListParameter list = l.cast(ListParameter.class);
		if ( list == null ) throw new FizzleException("list could not be operated on as TCL list.");

		String searchs = search.asString(ctx);

		if ( !all ) {
			for ( int i = 0; i < list.length(); ++i) {
				if ( list.index(i).asString(ctx).equals(searchs) ) {
					if ( inline ) return EvaluationResult.makeOkay(list.index(i));
					return EvaluationResult.makeOkay(Parameter.from(i));
				}
			}
			return EvaluationResult.makeOkay(Parameter.from(-1));
		} else { //All
			return null;
		}
	}
}
