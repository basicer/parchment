package com.basicer.parchment.base;

import com.basicer.parchment.Debug;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.FallingBlock;


import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;


public class Block extends OperationalSpell<BlockParameter> {

	@Override
	public String[] getAliases() { return new String[] {"b"}; }

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
	
	@Operation(aliases = {"type", "id"}, desc = "Change the type of target block to a new type.")
	public static Parameter materialOperation(org.bukkit.block.Block block, Context ctx, MaterialParameter type) {
		if ( type != null ) {
			Material m = type.asMaterial(ctx);
			if ( m == null ) fizzle("Unknown material " + type.asString());
			block.setType(m);
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

	public static Parameter midOperation(org.bukkit.block.Block block, Context ctx) {
		Location l = block.getLocation();
		l.add(0.5, 0, 0.5);
		return LocationParameter.from(l);
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

	private static Parameter rel(org.bukkit.block.Block block, BlockFace dir) {
		org.bukkit.block.Block bp = block.getRelative(dir);
		if ( bp == null ) {
			Debug.info("Coudlnt navigate");
		}
		return BlockParameter.from(bp);
	}

	@Operation(desc = "Return the block north of this block.")
	public static Parameter northOperation(org.bukkit.block.Block block, Context ctx) {
		return rel(block, BlockFace.NORTH);
	}
	
	@Operation(desc = "Return the block south of this block.")
	public static Parameter southOperation(org.bukkit.block.Block block, Context ctx) {
		return rel(block, BlockFace.SOUTH);
	}
	
	@Operation(desc = "Return the block east of this block.")
	public static Parameter eastOperation(org.bukkit.block.Block block, Context ctx) {
		return rel(block, BlockFace.EAST);
	}
	
	@Operation(desc = "Return the block west of this block.")
	public static Parameter westOperation(org.bukkit.block.Block block, Context ctx) {
		return rel(block, BlockFace.WEST);
	}
	
	@Operation(desc = "Return the block above this block.")
	public static Parameter upOperation(org.bukkit.block.Block block, Context ctx) {
		return rel(block, BlockFace.UP);
	}
	
	@Operation(desc = "Return the block below this block.")
	public static Parameter downOperation(org.bukkit.block.Block block, Context ctx) {
		return rel(block, BlockFace.DOWN);
	}

	@Operation(desc = "Return the block some distance from a block face.", argnames = {"direction", "amount"})
	public static Parameter relOperation(org.bukkit.block.Block block, Context ctx, StringParameter dir, IntegerParameter amount) {
		BlockFace face = dir.asEnum(BlockFace.class);
		if ( amount == null ) return Parameter.from(block.getRelative(face, 1));
		return Parameter.from(block.getRelative(face, amount.asInteger(ctx)));
	}

	public static Parameter textOperation(org.bukkit.block.Block block, Context ctx, StringParameter text) {
		String current;
		BlockState blockState = block.getState();
		if ( blockState instanceof Sign ) {
			Sign sign = (Sign) blockState;
			String[] lines = sign.getLines();
			StringBuilder b = new StringBuilder();
			for ( int i = 0; i < lines.length; ++ i ) {
				if ( i != 0 ) b.append("\n");
				b.append(lines[i]);
			}
			current = b.toString();
		} else {
			current = "";
		}

		if ( text == null ) return Parameter.from(current);
		String nue = text.asString(ctx);

		if ( blockState instanceof Sign ) {
			Sign sign = (Sign) blockState;
			String[] lines = nue.split("\n");
			for ( int i = 0; i < 4; ++i ) {
				if ( lines.length > i ) sign.setLine(i, lines[i]);
				else sign.setLine(i, "");
			}
			blockState.update();
		} else {
			fizzle("Block is not a sign?! " + block.getClass().getName());
		}

		return Parameter.from(nue);
	}

	@Operation(desc = "Cause a block update.  Warning: Tile Entities NBT may be /reset!")
	public static Parameter updateOperation(org.bukkit.block.Block block, Context ctx) {
		BlockState state = block.getState();
		block.setTypeIdAndData(0, block.getData(), false);
		state.update(true, true);
		return Parameter.from(block);
	}

	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
	}

}
