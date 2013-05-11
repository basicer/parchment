package com.basicer.parchment.base;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;


import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.annotations.Operation;
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
	

	public static Parameter typeNoPhysicsOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		if ( type != null ) {
			block.setTypeId(type.as(Material.class).getId(), false);
		}
		return Parameter.from(block.getType());
	}
	
	@Operation(aliases = {"type"}, desc = "Change the type of target block to a new type.")
	public static Parameter materialOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		if ( type != null ) {
			block.setType(type.asMaterial(ctx));
		}
		return Parameter.from(block.getType());
	}
	
	@Operation(desc = "Break target block as if broken by a player.")
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
	
	@Operation(desc = "Grow a tree of the given type on top of this block.  Returns 1 uppon success, 0 otherwise.")
	public static Parameter growtreeOperation(org.bukkit.block.Block block, Context ctx, StringParameter type) {
		TreeType t = TreeType.TREE;			
		if ( type != null ) t = type.asEnum(TreeType.class);
		if ( t == null ) fizzle("No such tree type: " + type.asString());
		return Parameter.from(block.getWorld().generateTree(block.getLocation().add(0, 1.0, 0), t) ? 1 : 0);
	}
	
	@Operation(desc = "Change target block into a falling block.  Return the new entity.")
	public static Parameter fallOperation(org.bukkit.block.Block block, Context ctx) {
		
		Material type = block.getType();
		byte data = block.getData();
		
		block.setType(Material.AIR);
		FallingBlock b = block.getWorld().spawnFallingBlock(block.getLocation(), type, data);
		
		return Parameter.from(b);
	}
	
	@Operation(desc = "Return the block north of this block.")
	public static Parameter northOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.NORTH));
	}
	
	@Operation(desc = "Return the block south of this block.")
	public static Parameter southOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.SOUTH));
	}
	
	@Operation(desc = "Return the block east of this block.")
	public static Parameter eastOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.EAST));
	}
	
	@Operation(desc = "Return the block west of this block.")
	public static Parameter westOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.WEST));
	}
	
	@Operation(desc = "Return the block above this block.")
	public static Parameter upOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.UP));
	}
	
	@Operation(desc = "Return the block below this block.")
	public static Parameter downOperation(org.bukkit.block.Block block, Context ctx) {
		return Parameter.from(block.getRelative(BlockFace.DOWN));
	}
	
	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
	}
	
	
}
