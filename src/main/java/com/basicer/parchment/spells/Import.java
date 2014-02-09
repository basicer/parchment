package com.basicer.parchment.spells;


import com.basicer.parchment.*;
import com.basicer.parchment.TargetedCommand;

public class Import extends TCLCommand {


	@Override
	public String[] getArguments() { return new String[] { "from" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String from = ctx.get("from").asString();
		TCLCommand cmd = ctx.getCommandFactory().get(from);
		
		if ( !(cmd instanceof TargetedCommand) ) return EvaluationResult.makeError("Cant import from TCLCommand");
		
		TargetedCommand s = (TargetedCommand) cmd;
		ctx.up(1).importProcs(s.getSpellContext());
		Debug.trace("After import up is now " + ctx.up(1).getDebuggingString());
		
		
		return EvaluationResult.OK;
	}
	
	

}