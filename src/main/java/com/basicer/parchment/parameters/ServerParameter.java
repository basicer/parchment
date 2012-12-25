package com.basicer.parchment.parameters;

import org.bukkit.Server;

import com.basicer.parchment.Context;

public class ServerParameter extends Parameter {

	private Server self;
	public ServerParameter(Server str) {
		self = str;
	}
	
	public Server asServer(Context ctx) { return self; }
}
