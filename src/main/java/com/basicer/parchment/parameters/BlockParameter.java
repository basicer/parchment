package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.basicer.parchment.Context;

public class BlockParameter extends Parameter {
	private Block self;
	private BlockFace extra;
	
	
	BlockParameter(Block self) {
		this.self = self;
	}
	
	BlockParameter(Block self, BlockFace extra) {
		this.self = self;
		this.extra = extra;
	}
	
	@Override
	public Class<Block> getUnderlyingType() { return Block.class; }

	
	@Override
	public Block asBlock(Context ctx) { return self; }
	
	@Override
	public Location asLocation(Context ctx, SelectionMode mode) {
		return self.getLocation();
	}
	
	@Override
	public String asString(Context ctx) {
		Location l = self.getLocation();
		return "[Block T:" + self.getType().name() + " @ " + self.getWorld().getName() + "/" 
				+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() +
				"]";
	}

	
}
