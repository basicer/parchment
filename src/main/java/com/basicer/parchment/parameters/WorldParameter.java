package com.basicer.parchment.parameters;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;


import com.basicer.parchment.Context;

public class WorldParameter extends Parameter {

	private World self;
	WorldParameter(World self) {
		this.self = self;
	}
	
	@Override
	public Class<World> getUnderlyingType() { return World.class; }
	
	
	public World asWorld(Context ctx) { return self; }

	@Override
	public String asString(Context ctx) { return self.getName(); }
	
	public static World asWorld(StringParameter str, Context ctx) {
		if ( ctx == null ) return null;		
		return Bukkit.getWorld(str.asString(ctx));		
	}

	public World asWorld() {
		return self;
	}


	public static WorldParameter from(World world) {
		return new WorldParameter(world);
	}

	public static WorldParameter castFrom(StringParameter from, Context ctx) {
		World w = Bukkit.getServer().getWorld(from.asString(ctx));
		if ( w == null ) return null;
		return new WorldParameter(w);

	}
	
}
