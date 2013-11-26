package com.basicer.parchment.base;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.bukkit.util.Vector;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.BlockParameter;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.EntityParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.LivingEntityParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;

public class Entity extends OperationalSpell<EntityParameter>  {


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
	
	public static Parameter addPotionOperation(org.bukkit.entity.Entity ent, Context ctx, StringParameter name, DoubleParameter dur, IntegerParameter power)
	{
		if ( !( ent instanceof LivingEntity )) fizzle("Operation requires living Entity");
		LivingEntity lent = (LivingEntity) ent;
		
		PotionEffectType eff = PotionEffectType.getByName(name.asString().toUpperCase());
		lent.addPotionEffect(eff.createEffect((int)(100 * dur.asDouble()), power.asInteger()));
		
		return Parameter.from(ent);
	}
	
	public static Parameter clearPotionsOperation(org.bukkit.entity.Entity ent, Context ctx) {
		if ( !( ent instanceof LivingEntity )) fizzle("Operation requires living Entity");
		LivingEntity lent = (LivingEntity) ent;
		for ( PotionEffect e : lent.getActivePotionEffects() ) {
			lent.removePotionEffect(e.getType());
		}
		return Parameter.from(ent);
	}
	

	@Operation(aliases = {"ignight"})
	public static Parameter igniteOperation(org.bukkit.entity.Entity ent, Context ctx) {
		ent.setFireTicks(10 * 15);
		return Parameter.from(ent);
	}
	
	@Operation(aliases = {"tp"}, desc = "Teleport target entity to a given location.  Return the new loaction.")
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

	@Operation(aliases = {"pos"}, desc = "Returns the entities position as a Location.")
	public static Parameter locationOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.getLocation());
	}

	public static Parameter targetOperation(org.bukkit.entity.Entity ent, Context ctx, EntityParameter target) {
		if (!(ent instanceof Creature)) fizzle("Entity needs to be a Creature.");
		Creature c = (Creature) ent;
		if ( target != null ) {
			LivingEntity le = target.asLivingEntity(ctx);
			if ( le == null ) fizzle("Target entity must be a living entity.");
			c.setTarget(le);
		}
		return Parameter.from(c.getTarget());
	}

	public static Parameter onGroundOperation(org.bukkit.entity.Entity ent, Context ctx) {
		return Parameter.from(ent.isOnGround() ? 1 : 0);
	}
	
	public static Parameter removeOperation(org.bukkit.entity.Entity ent, Context ctx) {
		ent.remove();
		return Parameter.EmptyString;
	}

	public static Parameter pickupdelayOperation(org.bukkit.entity.Entity ent, Context ctx, IntegerParameter ticks) {
		
		if (!(ent instanceof org.bukkit.entity.Item)) fizzle("Entity needs to be an Item Entity is " + ent.getClass().getSimpleName());
		org.bukkit.entity.Item i = (org.bukkit.entity.Item) ent;
		if ( ticks != null ) {
			i.setPickupDelay(ticks.asInteger(ctx));
		}
		return Parameter.from(i.getPickupDelay());
	}
	
}
