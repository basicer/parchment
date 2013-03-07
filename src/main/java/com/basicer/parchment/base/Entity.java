package com.basicer.parchment.base;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
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
import com.basicer.parchment.parameters.BlockParameter;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.EntityParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;

public class Entity extends OperationalSpell<EntityParameter>  {


	public Parameter affect(EntityParameter target, Context ctx) {
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
	
	public Parameter nameOperation(org.bukkit.entity.Entity ent, Context ctx) {
		String name = ent.getType().getName();
		if ( name == null ) return null;
		return Parameter.from(name);	
	}
	
	public Parameter stillOperation(org.bukkit.entity.Entity ent, Context ctx) {
		ent.setVelocity(new Vector(0,0,0));
		ent.setFallDistance(0.0f);
		return Parameter.from(true);
	}
	
	public Parameter addpotionOperation(org.bukkit.entity.Entity ent, Context ctx, StringParameter name, DoubleParameter dur, IntegerParameter power)
	{
		if ( !( ent instanceof LivingEntity )) fizzle("Operation requires living Entity");
		LivingEntity lent = (LivingEntity) ent;
		
		PotionEffectType eff = PotionEffectType.getByName(name.asString().toUpperCase());
		lent.addPotionEffect(eff.createEffect((int)(100 * dur.asDouble()), power.asInteger()));
		
		return Parameter.from(ent);
	}
	
	public Parameter clearpotionsOperation(org.bukkit.entity.Entity ent, Context ctx) {
		if ( !( ent instanceof LivingEntity )) fizzle("Operation requires living Entity");
		LivingEntity lent = (LivingEntity) ent;
		for ( PotionEffect e : lent.getActivePotionEffects() ) {
			lent.removePotionEffect(e.getType());
		}
		return Parameter.from(ent);
	}
	
	
	public Parameter tpOperation(org.bukkit.entity.Entity ent, Context ctx, Parameter location) {
		return teleportOperation(ent, ctx, location);
	}
	
	public Parameter teleportOperation(org.bukkit.entity.Entity ent, Context ctx, Parameter location) {
		LivingEntity lent = null;
		if ( ent instanceof LivingEntity ) lent = (LivingEntity) ent;
		
		if ( location != null ) {
			if ( location.as(Entity.class) != null ) {
				ent.teleport(location.as(org.bukkit.entity.Entity.class), TeleportCause.COMMAND);
			} else if ( location.as(Block.class) != null ) {
				org.bukkit.block.Block bloc = location.as(org.bukkit.block.Block.class);
				Location loc = bloc.getRelative(BlockFace.UP).getLocation();
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
			} else if ( location.as(Location.class) != null ) {
				Location loc = location.as(Location.class);
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(location.as(Location.class), TeleportCause.COMMAND);
			} else if ( location.asDouble() != null ) {
				if ( lent == null ) fizzle("Can only use distance based teleports on living entities");
				
				double distance = location.asDouble();
				List<org.bukkit.block.Block> blocks = lent.getLastTwoTargetBlocks(null, (int)distance + 1);
				org.bukkit.block.Block bloc = blocks.get(1);
				
				bloc = bloc.getRelative(BlockFace.UP);
				if ( !bloc.isEmpty() ) bloc = blocks.get(0);
				
				Location loc = bloc.getLocation();
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
				
			} else if ( location.asString() != null ) {
				String l = location.asString();
				if ( l.equals("spawn") ) {
					ent.teleport(ent.getWorld().getSpawnLocation(), TeleportCause.COMMAND);
				} else if ( l.equals("home") ) {
					
					if (!(ent instanceof Player)) fizzle("Only players have homes.");
					ent.teleport(((Player) ent).getBedSpawnLocation(), TeleportCause.COMMAND);
				}
						
			}
		}
		
		return Parameter.from(ent.getLocation());
	}
	
	public Parameter clearOperation(org.bukkit.entity.Entity ent, Context ctx) {
		Player pent = toPlayer(ent);
		pent.getInventory().clear();
		return Parameter.from(pent);
	}
	
	
	protected LivingEntity toLivingEntity(org.bukkit.entity.Entity ent) {
		if ( ent instanceof LivingEntity ) return (LivingEntity)ent;
		fizzle("Operation only valid on Living Entities");
		return null;
	}
	
	protected Player toPlayer(org.bukkit.entity.Entity ent) {
		if ( ent instanceof Player ) return (Player)ent;
		fizzle("Operation only valid on Players");
		return null;
	}
	
}
