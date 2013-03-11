package com.basicer.parchment.base;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.parameters.*;


public class Block extends OperationalSpell<BlockParameter> {


	public Parameter affect(BlockParameter target, Context ctx) {
		org.bukkit.block.Block block = target.as(org.bukkit.block.Block.class);
		if ( block == null ) fizzleTarget("Not an block.");
		return this.doaffect(target, ctx);
 	}
	
	public Parameter affect(LocationParameter target, Context ctx) {
		World w  = ctx.getWorld();
		if ( w == null ) fizzleTarget("No world to resolve location target");
		org.bukkit.block.Block block = w.getBlockAt(target.as(Location.class));
		if ( block == null ) fizzleTarget("Not an block.");
		return this.doaffect((BlockParameter)Parameter.from(block), ctx);
 	}
	


	public static Parameter materialOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		if ( type != null ) {
			block.setType(type.as(Material.class));
		}
		return Parameter.from(block.getType());
	}
	
	
	public static Parameter typeOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		return materialOperation(block, ctx, type);
	}
	
	public static Parameter breakOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.breakNaturally());
	}

	public static Parameter powerOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getBlockPower());
	}
	
	
	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.FuzzyMatch;
	}
	
	
}
