package com.basicer.parchment.parameters;

import org.bukkit.Location;

import com.basicer.parchment.Context;

public class LocationParameter extends Parameter {
	private Location self;
	LocationParameter(Location self) {
		this.self = self;
	}
	@Override
	public Location asLocation(Context ctx, SelectionMode mode) {
		return self;
	}
	@Override
	public String asString(Context ctx) {
		return "[Location T:" + self.getWorld().getName() + "/" 
				+ self.getBlockX() + "," + self.getBlockY() + "," + self.getBlockZ() +
				"]";
	}
	
	
}