package com.basicer.parchment.base;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.entity.EntityType;


import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.Spell.FirstParamaterTargetType;
import com.basicer.parchment.parameters.*;


public class World extends OperationalSpell<WorldParameter> {

	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.ExactMatch;
	}

	

	public Parameter affect(WorldParameter target, Context ctx) {
		org.bukkit.World w = target.asWorld(ctx);
		return this.doaffect(target, ctx);
 	}
	


	public static Parameter growtreeOperation(org.bukkit.World world, Context ctx, LocationParameter where, StringParameter type) {
		TreeType t = TreeType.TREE;			
		if ( type != null ) t = type.asEnum(TreeType.class);
		
		return Parameter.from(world.generateTree(where.asLocation(ctx), t) ? 1 : 0);
	}
	
	public static Parameter spawnOperation(org.bukkit.World world, Context ctx, StringParameter type, LocationParameter where) {
		world.spawnEntity(where.asLocation(ctx), (EntityType)type.asEnum(EntityType.class));
		return where;
	}



	@Override
	public DefaultTargetType getDefaultTargetType(Context ctx, String source) {
		return DefaultTargetType.World;
	}
	

	
}
