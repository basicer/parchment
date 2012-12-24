package com.basicer.parchment.base;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class SCommand extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "command" }; }
	
	@Override
	public Parameter execute(Context ctx) {
		Parameter cmd = ctx.get("command");
		CommandSender sender = ctx.getCaster().asPlayer(); 
		Server s = ctx.getServer().asServer();
		
		boolean okay = s.dispatchCommand(sender, cmd.asString());
		
		return Parameter.from(okay ? "true" : "false");
	}

}
