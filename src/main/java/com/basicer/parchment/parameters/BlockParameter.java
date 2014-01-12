package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.basicer.parchment.Context;
import org.bukkit.util.Vector;

public class BlockParameter extends Parameter {
	private Block self;
	private BlockFace extra;
	
	BlockParameter(Block self) {
		this.self = self;
	}
	
	@Override
	public Class<Block> getUnderlyingType() { return Block.class; }

	
	
	public Block asBlock(Context ctx) { return self; }
	
	
	public Location asLocation(Context ctx) {
		return self.getLocation();
	}

	public Vector asVector(Context ctx) {
		return self.getLocation().toVector();
	}


	public World asWorld(Context ctx) {
		return self.getLocation().getWorld();
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
	
	protected void addExtraCastParameters(Object[] extra) {
		if ( extra.length != 1 ) return;
		if ( extra[0] instanceof BlockFace ) {
			BlockFace b = (BlockFace) extra[0];
			this.extra = b;
		} else if ( extra[0] instanceof Block ) { 
			this.extra = ((Block) extra[0]).getFace(self);
		
		}
	}
}
