package com.basicer.parchment.parameters;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldParameter extends Parameter {

	private World self;
	WorldParameter(World self) {
		this.self = self;
	}
	
	@Override
	public World asWorld() { return self; }

	@Override
	public String asString() { return self.getName(); }
}
