package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Affect extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "type", "args" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
	
		Parameter t = ctx.resolve("this");
		if ( t == null ) return EvaluationResult.makeError("Affect: Cant find $this");
		TCLCommand tt = t.as(TCLCommand.class);
		if ( tt == null ) return EvaluationResult.makeError("Affect: $this isnt a delegate.");
		
		if ( !( tt instanceof ScriptedSpell )) return EvaluationResult.makeError("Affect: $this isnt a ScriptedSpell.");

		ScriptedSpell spell = (ScriptedSpell) tt;
		return spell.executeBinding(ctx.get("type").toString(), ctx.createSubContext(), engine);
	
		
	}

}
