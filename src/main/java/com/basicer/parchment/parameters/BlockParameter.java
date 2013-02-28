package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.basicer.parchment.Context;

public class BlockParameter extends Parameter {
	private Block self;
	
	
	BlockParameter(Block self) {
		this.self = self;
	}
	
	@Override
	public Class<Block> getUnderlyingType() { return Block.class; }

	
	
	public Block asBlock(Context ctx) { return self; }
	
	
	public Location asLocation(Context ctx) {
		return self.getLocation();
	}
	
	public String asString(Context ctx) {
		Location l = self.getLocation();
		return "[Block T:" + self.getType().name() + " @ " + self.getWorld().getName() + "/" 
				+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() +
				"]";
	}

	public Material asMaterial(Context ctx) {
		return self.getType();
	}
}
