package com.basicer.parchment.base;

import java.lang.reflect.Field;
import java.util.List;

import com.basicer.parchment.FizzleException;
import com.basicer.parchment.OperationalTargetedCommand;
import com.basicer.parchment.Reflect;
import com.basicer.parchment.parameters.*;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

import com.basicer.parchment.Context;
import com.basicer.parchment.annotations.Operation;

public class Entity extends OperationalTargetedCommand<EntityParameter> {

	@Override
	public String[] getAliases() {
		return new String[] { "ent" };
	}

	public Parameter affect(EntityParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	public Parameter affect(LivingEntityParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	
	public Parameter affect(PlayerParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	public Parameter affect(LocationParameter target, Context ctx) {
		for ( org.bukkit.entity.Entity e : target.as(Location.class).getWorld().getEntities() ) {
			if ( e.getLocation().distanceSquared(target.as(Location.class)) < 4 ) {
				return this.doaffect((EntityParameter)Parameter.from(e), ctx);
			}
		}
		
		fizzle("No entities fond there");
		return null;
	}
	
	public Parameter affect(BlockParameter target, Context ctx) {
		return affect(target.cast(LocationParameter.class), ctx);
	}

	public static org.bukkit.entity.Entity create(Context ctx, StringParameter type, LocationParameter where) {
		//where.asWorld(ctx).spawn(where.asLocation(ctx), Class.forName(type));
		org.bukkit.World world = where.asWorld(ctx);

		org.bukkit.entity.EntityType etype = type.asEnum(org.bukkit.entity.EntityType.class);
		if ( etype == null ) fizzle("No such entity type: " + type.asString(ctx));
		if ( etype.isSpawnable() ) fizzle("Entity type is not spawnable: " + etype.toString());
		try {
			return world.spawnEntity(where.asLocation(ctx), etype);
		} catch ( IllegalArgumentException ex ) {
			fizzle(ex.getMessage());
			return null;
		}

	}

	@Operation(desc = "Return target entity's name.")
	public static Parameter nameOperation(org.bukkit.entity.Entity ent, Context ctx) {
		String name = ent.getType().getName();
		if ( name == null ) return null;
		return Parameter.from(name);	
	}
	

	@Operation(desc = "Set target entity's velocity to zero and reset fall distance.")
	public static Parameter stillOperation(org.bukkit.entity.Entity ent, Context ctx) {
		ent.setVelocity(new Vector(0,0,0));
		ent.setFallDistance(0.0f);
		return Parameter.from(true);
	}


	@Operation(aliases={"vel"})
	public static Parameter velocityOperation(org.bukkit.entity.Entity ent, Context ctx, VectorParameter set) {
		if ( set != null ) 	ent.setVelocity(set.asVector(ctx));
		return VectorParameter.from(ent.getVelocity());
	}

	@Operation(aliases={"addvel"})
	public static Parameter addVelocityOperation(org.bukkit.entity.Entity ent, Context ctx, VectorParameter add) {
		if ( add == null ) fizzle("No velocity to add?!");
		Vector v = ent.getVelocity();
		v.add(add.asVector(ctx));
		ent.setVelocity(v);
		return VectorParameter.from(v);
	}


	@Operation(aliases = {"ignight"})
	public static Parameter igniteOperation(org.bukkit.entity.Entity ent, Context ctx) {
		ent.setFireTicks(20 * 15);
		return Parameter.from(ent);
	}

	public static Parameter nerfaiOperation(org.bukkit.entity.Entity ent, Context ctx, BooleanParameter set) {

		try {
			Object o = Reflect.invokeMethod(ent, Object.class, "getHandle");
			Field f = Reflect.getField(o, "fromMobSpawner");
			if ( set != null ) f.setBoolean(o, set.asBoolean(ctx));
			return Parameter.from(f.getBoolean(o));
		} catch (IllegalAccessException e1) {
			throw new FizzleException(e1.getMessage());
		}

	}

	@Operation(aliases = {"tp"}, desc = "Teleport target entity to given location.  Return the new loaction.")
	public static Parameter teleportOperation(org.bukkit.entity.Entity ent, Context ctx, Parameter location) {
		LivingEntity lent = null;
		if ( ent instanceof LivingEntity ) lent = (LivingEntity) ent;
		if ( location != null ) {
			if ( location.as(Entity.class) != null ) {
				ent.teleport(location.as(org.bukkit.entity.Entity.class), TeleportCause.COMMAND);
			} else if ( location.getClass() != BlockParameter.class && location.as(Location.class) != null ) {
				Location loc = location.as(Location.class);
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
			} else if ( location.getClass() != BlockParameter.class && location.cast(LocationParameter.class, ctx) != null ) {
				LocationParameter locp = location.cast(LocationParameter.class, ctx);
				Location loc = locp.asLocation(ctx);
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
			} else if ( location.as(org.bukkit.block.Block.class) != null ) {
				ctx.sendDebugMessage("4");
				org.bukkit.block.Block bloc = location.as(org.bukkit.block.Block.class);
				Location loc = bloc.getRelative(BlockFace.UP).getLocation();
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
			} else if ( location.asDouble() != null ) {
				ctx.sendDebugMessage("6");
				if ( lent == null ) fizzle("Can only use distance based teleports on living entities");

				double distance = location.asDouble();
				List<org.bukkit.block.Block> blocks = lent.getLastTwoTargetBlocks(null, (int)distance + 1);
				org.bukkit.block.Block bloc = blocks.size() > 1 ? blocks.get(1) : blocks.get(0);

				//TODO: Backtrace this from the end point so we can go thoutypegh things?

				bloc = bloc.getRelative(BlockFace.UP);
				if ( !bloc.isEmpty() ) bloc = blocks.get(0);

				Location loc = bloc.getLocation();
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
			} else if ( location.as(Player.class) != null ) {
				Player p = location.as(Player.class);
				ent.teleport(p, TeleportCause.COMMAND);
			} else if ( location.asString() != null ) {
				ctx.sendDebugMessage("7");
				String l = location.asString();
				if ( l.equals("spawn") ) {
					ent.teleport(ent.getWorld().getSpawnLocation(), TeleportCause.COMMAND);
				} else if ( l.equals("home") ) {
					
					if (!(ent instanceof Player)) fizzle("Only players have homes.");
					ent.teleport(((Player) ent).getBedSpawnLocation(), TeleportCause.COMMAND);
				}
				fizzle("Couldn't figure out what to do with " + location.asString());
			}
		}

		
		return Parameter.from(ent.getLocation());
	}


	@Operation(aliases = {"pos", "loc"}, desc = "Returns the entities position as a Location.")
	public static Parameter locationOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.getLocation());
	}


	@Operation(aliases = {"posx", "locx"}, desc = "Returns the entities x position as a number.")
	public static Parameter locationXOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.getLocation().getX());
	}


	@Operation(aliases = {"posy", "locy"}, desc = "Returns the entities y position as a number.")
	public static Parameter locationYOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.getLocation().getY());
	}


	@Operation(aliases = {"posz", "locz"}, desc = "Returns the entities x position as a number.")
	public static Parameter locationZOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.getLocation().getZ());
	}

	@Operation(aliases = {"posv", "locv"}, desc = "Returns the entities position as a Vector.")
	public static Parameter locationVectorOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.getLocation().toVector());
	}

	@Operation(aliases = {"dirv"}, desc = "Returns the entities position as a unit Vector.")
	public static Parameter directionVectorOperation(org.bukkit.entity.Entity ent, Context ctx) {
		Location l = ent.getLocation();
		return VectorParameter.from(l.getDirection());
	}

	@Operation(aliases = {"rotv"}, desc = "Returns the entities position as a unit Vector.")
	public static Parameter rotationVectorOperation(org.bukkit.entity.Entity ent, Context ctx) {
		Location l = ent.getLocation();
		double yaw = Math.toRadians(l.getYaw() + 90);
		Vector v = new Vector();
		v.setZ(Math.sin(yaw));
		v.setX(Math.cos(yaw));
		return VectorParameter.from(v);
	}




	public static Parameter onGroundOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.isOnGround() ? 1 : 0);
	}
	
	public static Parameter removeOperation(org.bukkit.entity.Entity ent, Context ctx) {
		ent.remove();
		return Parameter.EmptyString;
	}

	public static Parameter uuidOperation(org.bukkit.entity.Entity ent, Context ctx) {

		return StringParameter.from(ent.getUniqueId().toString());
	}


	public static Parameter pickupdelayOperation(org.bukkit.entity.Entity ent, Context ctx, IntegerParameter ticks) {
		
		if (!(ent instanceof org.bukkit.entity.Item)) fizzle("Entity needs to be an Item Entity is " + ent.getClass().getSimpleName());
		org.bukkit.entity.Item i = (org.bukkit.entity.Item) ent;
		if ( ticks != null ) {
			i.setPickupDelay(ticks.asInteger(ctx));
		}
		return Parameter.from(i.getPickupDelay());
	}

	public static Parameter setPassengerOperation(org.bukkit.entity.Entity e, Context ctx, EntityParameter other) {
		return BooleanParameter.from(e.setPassenger(other.asEntity(ctx)));
	}

}
