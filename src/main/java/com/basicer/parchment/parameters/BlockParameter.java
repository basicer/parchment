package com.basicer.parchment.parameters;

import org.bukkit.block.Block;

import com.basicer.parchment.Context;

public class BlockParameter extends Parameter {
	private Block self;
	BlockParameter(Block self) {
		this.self = self;
	}
	
	@Override
	public Block asBlock(Context ctx) { return self; }
}
