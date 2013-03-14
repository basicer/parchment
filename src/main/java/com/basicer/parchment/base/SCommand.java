package com.basicer.parchment.base;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		CommandSender sender = ctx.getCaster().as(Player.class); 
		Server s = ctx.getServer();
		
		//boolean okay = s.dispatchCommand(sender, cmd.asString());
		boolean okay = s.dispatchCommand(ctx.getServer().getConsoleSender(), cmd.asString());
		
		return new EvaluationResult(Parameter.from(okay));
	}

}
