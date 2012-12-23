package com.basicer.parchment.parameters;

import org.bukkit.Server;

public class ServerParameter extends Parameter {

	private Server self;
	public ServerParameter(Server str) {
		self = str;
	}
	
	public Server asServer() { return self; }
}
