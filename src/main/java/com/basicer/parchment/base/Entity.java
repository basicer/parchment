package com.basicer.parchment.base;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.EntityParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;

public class Entity extends Spell  {


	@Override
	public String[] getArguments() { return new String[] { "operation?", "args" }; }
	
	public Parameter affect(EntityParameter target, Context ctx) {
		return operation(target, ctx);
	}
	
	public Parameter affect(PlayerParameter target, Context ctx) {
		return operation(target, ctx);
	}
	
	public Parameter operation(Parameter target, Context ctx) {
		org.bukkit.entity.Entity ent = target.asEntity();
		if ( ent == null ) fizzleTarget("Not an entity.");
		LivingEntity lent = null;
		Player pent = null;
		
		if ( ent instanceof LivingEntity ) lent = (LivingEntity) ent;
		if ( ent instanceof Player ) pent = (Player) ent;
		
		
		ArrayList<Parameter> args = ctx.getArgs();
		Parameter pop = ctx.get("operation");
		
		if ( pop == null ) return Parameter.from(ent);
		String op = pop.asString(ctx);
		
		if ( op.equals("name") ) {
			/*
			if ( ctx.hasArgs() ) {
				m.setDisplayName(getArgOrFizzle(ctx, 0, StringParameter.class).asString());
				itm.setItemMeta(m);
			}
			*/
			String name = ent.getType().getName();
			if ( name == null ) return null;
			return Parameter.from(name);
		} else if ( op.equals("teleport") ) {
			return teleportOpreation(ent, ctx);
		} else if ( op.equals("still") ) {
			ent.setVelocity(new Vector(0,0,0));
			ent.setFallDistance(0.0f);
			return Parameter.from(true);
		}
		
		
		
		fizzle("Invalid operation " + op);
		return null; //Never executed.
 	}
	
	
	public Parameter teleportOpreation(org.bukkit.entity.Entity ent, Context ctx) {
		LivingEntity lent = null;
		if ( ent instanceof LivingEntity ) lent = (LivingEntity) ent;
		
		if ( ctx.hasArgs() ) {
			Parameter location = ctx.getArgs().get(0);
			if ( location.asEntity() != null ) {
				ent.teleport(location.asEntity(), TeleportCause.COMMAND);
			} else if ( location.asLocation() != null ) {
				Location loc = location.asLocation();
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(location.asLocation(), TeleportCause.COMMAND);
			} else if ( location.asDouble() != null ) {
				if ( lent == null ) fizzle("Can only use distance based teleports on living entities");
				
				double distance = location.asDouble();
				List<org.bukkit.block.Block> blocks = lent.getLastTwoTargetBlocks(null, (int)distance + 1);
				Location loc = blocks.get(0).getLocation();
				loc.setPitch(ent.getLocation().getPitch());
				loc.setYaw(ent.getLocation().getYaw());
				ent.teleport(loc, TeleportCause.COMMAND);
				
			}
		}
		
		return Parameter.from(ent.getLocation());
	}
	
	
}
