package com.basicer.parchment.base;


import org.bukkit.Location;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.Spell.FirstParamaterTargetType;

import com.basicer.parchment.parameters.*;

public class Server extends OperationalSpell<ServerParameter> {
	
	public Class<? extends OperationalSpell<?>> getBaseClass() { return null; }
	
	
	
	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.Never;
	}




	public Parameter affect(ServerParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	public Parameter affect(PlayerParameter target, Context ctx) {
		return this.doaffect(target.cast(ServerParameter.class), ctx);
	}
	
	
	public static Parameter motdOperation(org.bukkit.Server server, Context ctx) {
		return Parameter.from(server.getMotd());
	}
	
	public static Parameter nameOperation(org.bukkit.Server server, Context ctx) {
		return Parameter.from(server.getName());
	}
	
	public static Parameter shutdownOperation(org.bukkit.Server server, Context ctx) {
		server.shutdown();
		return Parameter.EmptyString;
	}
	
	public static Parameter reloadOperation(org.bukkit.Server server, Context ctx) {
		server.reload();
		return Parameter.EmptyString;
	}
		
}
