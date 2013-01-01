package com.basicer.parchment.parameters;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;

public class WorldParameter extends Parameter {

	private World self;
	WorldParameter(World self) {
		this.self = self;
	}
	
	@Override
	public Class getUnderlyingType() { return World.class; }
	
	@Override
	public World asWorld(Context ctx) { return self; }

	@Override
	public String asString(Context ctx) { return self.getName(); }
}
