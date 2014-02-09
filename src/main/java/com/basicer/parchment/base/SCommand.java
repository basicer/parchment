package com.basicer.parchment.base;

import org.bukkit.Bukkit;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class SCommand extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "command" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		Parameter cmd = ctx.get("command");

		//boolean okay = s.dispatchCommand(sender, cmd.asString());
		boolean okay = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.asString());
		
		return EvaluationResult.makeOkay(Parameter.from(okay));
	}

}
