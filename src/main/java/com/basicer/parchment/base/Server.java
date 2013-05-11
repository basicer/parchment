package com.basicer.parchment.base;


import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;

import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;

public class Server extends OperationalSpell<ServerParameter> {
	
	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.Never;
	}




	public Parameter affect(ServerParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	public Parameter affect(PlayerParameter target, Context ctx) {
		return this.doaffect(target.cast(ServerParameter.class), ctx);
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

	@Operation(desc = "Send some string to all connected players.")
	public static Parameter broadcastOperation(org.bukkit.Server server, Context ctx, StringParameter arg1) {
		String str = arg1.asString(ctx).replace("%", "§");
	
		for ( org.bukkit.entity.Player p : server.getOnlinePlayers() ) {
			p.sendRawMessage(str);
		}
		
		return Parameter.EmptyString;
		
	}
}
