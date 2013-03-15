package com.basicer.parchment.spells;


import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Import extends TCLCommand {


	@Override
	public String[] getArguments() { return new String[] { "from" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String from = ctx.get("from").asString();
		TCLCommand cmd = ctx.getSpellFactory().get(from);
		
		if ( !(cmd instanceof Spell) ) return EvaluationResult.makeError("Cant import from TCLCommand");
		
		Spell s = (Spell) cmd;
		ctx.up(1).importProcs(s.getSpellContext());
		Debug.trace("After import up is now " + ctx.up(1).getDebuggingString());
		
		
		return EvaluationResult.OK;
	}
	
	

}