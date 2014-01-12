package com.basicer.parchment.base;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.TreeType;
import org.bukkit.entity.EntityType;


import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;


public class World extends OperationalSpell<WorldParameter> {

	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.ExactMatch;
	}

	

	public Parameter affect(WorldParameter target, Context ctx) {
		org.bukkit.World w = target.asWorld(ctx);
		return this.doaffect(target, ctx);
 	}
	


	public static Parameter growtreeOperation(org.bukkit.World world, Context ctx, LocationParameter where, StringParameter type) {
		TreeType t = TreeType.TREE;			
		if ( type != null ) t = type.asEnum(TreeType.class);
		if ( t == null ) fizzle("No such tree type: " + type.asString());
		return Parameter.from(world.generateTree(where.asLocation(ctx), t) ? 1 : 0);
	}
	
	@Operation(desc = "Spawn an entity of the given type at some location.  Returns the entity.")
	public static Parameter spawnOperation(org.bukkit.World world, Context ctx, StringParameter type, LocationParameter where) {
		org.bukkit.entity.Entity e = world.spawnEntity(where.asLocation(ctx), (EntityType)type.asEnum(EntityType.class));
		return Parameter.from(e);
	}

	public static Parameter soundOperation(org.bukkit.World world, Context ctx, StringParameter type, LocationParameter where, DoubleParameter volume, DoubleParameter pitch) {
		if ( where == null ) where = ctx.getCaster().cast(LocationParameter.class);
		if ( volume == null ) volume = Parameter.from(1.0);
		if ( pitch == null ) pitch = Parameter.from(1.0);
		
		//world.playEffect(where.asLocation(ctx), type.asEnum(Effect.class), 1);
		world.playSound(where.asLocation(ctx), type.asEnum(Sound.class),volume.asDouble().floatValue(), pitch.asDouble().floatValue());
		return type;
	}

	public static Parameter effectOperation(org.bukkit.World world, Context ctx, StringParameter type, LocationParameter where, IntegerParameter data, IntegerParameter radius) {
		if ( where == null ) where = ctx.getCaster().cast(LocationParameter.class);
		if ( data == null ) data = Parameter.from(1);
		
		if ( radius == null ) world.playEffect(where.asLocation(ctx), type.asEnum(Effect.class), data.asInteger().intValue());
		else world.playEffect(where.asLocation(ctx), type.asEnum(Effect.class), data.asInteger().intValue(), radius.asInteger().intValue());
		return type;
	}

	public static Parameter pvpOperation(org.bukkit.World world, Context ctx, BooleanParameter type) {
		if ( type != null ) world.setPVP(type.asBoolean());
		return Parameter.from(world.getPVP());
	}

	public static Parameter playersOperation(org.bukkit.World world, Context ctx) {
		ArrayList<Parameter> players = new ArrayList<Parameter>();
		for ( org.bukkit.entity.Player p : world.getPlayers() ) players.add(Parameter.from(p));
		return ListParameter.from(players);
	}

	public static Parameter spawnpointOperation(org.bukkit.World world, Context ctx, LocationParameter where) {
		if ( where != null ) {
			Location l = where.asLocation(ctx);
			world.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		}
		return Parameter.from(world.getSpawnLocation());
	}

	public static Parameter saveOperation(org.bukkit.World world, Context ctx) {
		world.save();
		return WorldParameter.from(world);
	}

	public static Parameter strikeLightningOperation(org.bukkit.World world, Context ctx, LocationParameter where) {
		world.strikeLightning(where.asLocation(ctx));
		return WorldParameter.from(world);
	}

	public static Parameter autosaveOperation(org.bukkit.World world, Context ctx, BooleanParameter set) {
		if ( set != null ) world.setAutoSave(set.asBoolean(ctx));
		return BooleanParameter.from(world.isAutoSave());
	}




	@Override
	public DefaultTargetType getDefaultTargetType(Context ctx, String source) {
		return DefaultTargetType.World;
	}
	

	
}
