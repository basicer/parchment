package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.basicer.parchment.Context;

public class BlockParameter extends Parameter {
	private Block self;
	BlockParameter(Block self) {
		this.self = self;
	}
	
	@Override
	public Block asBlock(Context ctx) { return self; }
	
	@Override
	public Location asLocation(Context ctx, SelectionMode mode) {
		return self.getLocation();
	}
	
	@Override
	public String asString(Context ctx) {
		return "[Block T:" + self.getType().name() + " @ " + self.getWorld().getName() + "]";
	}
}
