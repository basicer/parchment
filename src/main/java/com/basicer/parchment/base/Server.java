package com.basicer.parchment.base;


import com.basicer.parchment.Context;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.OperationalSpell;

import com.basicer.parchment.Spell;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class Server extends OperationalSpell<ServerParameter> {
	
	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.Never;
	}


	public Parameter affect(ServerParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	@Operation(desc = "Returns the current server MOTD.")
	public static Parameter motdOperation(org.bukkit.Server server, Context ctx) {
		return Parameter.from(server.getMotd());
	}
	
	@Operation(desc = "Returns the current server name.")
	public static Parameter nameOperation(org.bukkit.Server server, Context ctx) {
		return Parameter.from(server.getName());
	}
	
	@Operation(desc = "Shutdown the server.")
	public static Parameter shutdownOperation(org.bukkit.Server server, Context ctx) {
		server.shutdown();
		return Parameter.EmptyString;
	}
	
	@Operation(desc = "Tell bukkit to reload.  Same as the /reload command.")
	public static Parameter reloadOperation(org.bukkit.Server server, Context ctx) {
		server.reload();
		return Parameter.EmptyString;
	}

	public static Parameter playersOperation(org.bukkit.Server server, Context ctx) {
		ArrayList<Parameter> players = new ArrayList<Parameter>();
		for ( org.bukkit.entity.Player p : server.getOnlinePlayers() ) players.add(PlayerParameter.from(p));
		return ListParameter.from(players);
	}


	public static Parameter worldsOperation(org.bukkit.Server server, Context ctx) {
		ArrayList<Parameter> worlds = new ArrayList<Parameter>();
		for ( org.bukkit.World p : server.getWorlds() ) worlds.add(WorldParameter.from(p));
		return ListParameter.from(worlds);
	}

	@Operation(aliases = {"nchunks"})
	public static Parameter loadedChunkCountOperation(org.bukkit.Server server, Context ctx) {
		int count = 0;
		for ( org.bukkit.World w : server.getWorlds() ) count += w.getLoadedChunks().length;
		return IntegerParameter.from(count);
	}

	@Operation(desc = "Send some string to all connected players.")
	public static Parameter broadcastOperation(org.bukkit.Server server, Context ctx, StringParameter what) {

		String str = "null";
		if ( what != null ) str = what.asString(ctx).replace("%", "§");
	
		for ( org.bukkit.entity.Player p : server.getOnlinePlayers() ) {
			p.sendRawMessage(str);
		}
		
		return Parameter.EmptyString;
		
	}
}
