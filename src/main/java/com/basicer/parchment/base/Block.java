package com.basicer.parchment.base;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;


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
		World w  = target.asLocation(ctx).getWorld();
		if ( w == null ) w = ctx.getWorld();
		if ( w == null ) fizzleTarget("No world to resolve location target");
		org.bukkit.block.Block block = w.getBlockAt(target.as(Location.class));
		if ( block == null ) fizzleTarget("Not an block.");
		return this.doaffect((BlockParameter)Parameter.from(block), ctx);
 	}
	

	public static Parameter typeOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		return materialOperation(block, ctx, type);
	}
	
	public static Parameter typeNoPhysicsOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		if ( type != null ) {
			block.setTypeId(type.as(Material.class).getId(), false);
		}
		return Parameter.from(block.getType());
	}
	
	public static Parameter materialOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		if ( type != null ) {
			block.setType(type.asMaterial(ctx));
		}
		return Parameter.from(block.getType());
	}
	
	public static Parameter breakOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.breakNaturally());
	}

	public static Parameter powerOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getBlockPower());
	}
	
	public static Parameter dataOperation(org.bukkit.block.Block block, Context ctx, IntegerParameter data) {
		if ( data != null ) {
			block.setData(data.asInteger().byteValue());
		}
		return Parameter.from(block.getData());
	}
	
	public static Parameter growtreeOperation(org.bukkit.block.Block block, Context ctx, StringParameter type) {
		TreeType t = TreeType.TREE;			
		if ( type != null ) t = type.asEnum(TreeType.class);
		
		return Parameter.from(block.getWorld().generateTree(block.getLocation().add(0, 1.0, 0), t) ? 1 : 0);
	}
	
	public static Parameter fallOperation(org.bukkit.block.Block block, Context ctx) {
		
		Material type = block.getType();
		byte data = block.getData();
		
		block.setType(Material.AIR);
		FallingBlock b = block.getWorld().spawnFallingBlock(block.getLocation(), type, data);
		
		return Parameter.from(b);
	}
	
	
	public static Parameter northOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.NORTH));
	}
	public static Parameter southOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.SOUTH));
	}
	public static Parameter eastOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.EAST));
	}
	public static Parameter westOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.WEST));
	}
	public static Parameter upOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.UP));
	}
	public static Parameter downOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.DOWN));
	}
	
	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.FuzzyMatch;
	}
	
	
}
