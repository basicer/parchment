package com.basicer.parchment.base;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.BlockParameter;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;

public class Block extends Spell {


	@Override
	public String[] getArguments() { return new String[] { "operation?", "args" }; }
	
	public Parameter affect(BlockParameter target, Context ctx) {
		org.bukkit.block.Block block = target.asBlock();
		if ( block == null ) fizzleTarget("Not an block.");
		return action(block, ctx);
 	}
	
	public Parameter affect(LocationParameter target, Context ctx) {
		World w  = ctx.getWorld();
		if ( w == null ) fizzleTarget("No world to resolve location target");
		org.bukkit.block.Block block = w.getBlockAt(target.asLocation());
		if ( block == null ) fizzleTarget("Not an block.");
		return action(block, ctx);
 	}

	protected Parameter action(org.bukkit.block.Block block, Context ctx) {
		Parameter pop = ctx.get("operation");
		ArrayList<Parameter> args = ctx.getArgs();

		
		if ( pop == null ) return Parameter.from(block);
		String op = pop.asString();
		
		
		if ( op.equals("material") || op.equals("type") ) {
			Material old = block.getType();
			if ( args.size() > 0 ) {
				Material now = getArgOrFizzle(ctx, 0, MaterialParameter.class).asMaterial();
				block.setType(now);
			}
			return Parameter.from(block.getType());
		} else if ( op.equals("break") ) {
			return Parameter.from(block.breakNaturally());
		} else if ( op.equals("power") ) {
			return Parameter.from(block.getBlockPower());
		} 
		
		
		
		fizzle("Invalid operation " + op);
		return null;
		
	}

	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.FuzzyMatch;
	}
	
	
}
