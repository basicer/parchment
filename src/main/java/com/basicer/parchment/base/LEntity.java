package com.basicer.parchment.base;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.basicer.parchment.annotations.Operation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EntityEquipment;
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
import com.basicer.parchment.parameters.LivingEntityParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;

public class LEntity extends OperationalSpell<EntityParameter>  {

	public static Class<? extends OperationalSpell<?>> getBaseClass() { return Entity.class; }

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
	

	
	public static Parameter addpotionOperation(org.bukkit.entity.LivingEntity lent, Context ctx, StringParameter name, DoubleParameter dur, IntegerParameter power)
	{	
		PotionEffectType eff = PotionEffectType.getByName(name.asString().toUpperCase());
		lent.addPotionEffect(eff.createEffect((int)(100 * dur.asDouble()), power.asInteger()));
		
		return Parameter.from(lent);
	}
	
	public static Parameter clearpotionsOperation(org.bukkit.entity.LivingEntity lent, Context ctx) {
		for ( PotionEffect e : lent.getActivePotionEffects() ) {
			lent.removePotionEffect(e.getType());
		}
		return Parameter.from(lent);
	}
	
	
	public static Parameter holdOperation(org.bukkit.entity.LivingEntity le, Context ctx, Parameter set) {
		EntityEquipment pent = le.getEquipment();
		if ( set != null ) {
			if ( set instanceof ItemParameter) {
				pent.setItemInHand(((ItemParameter) set).asItemStack(ctx));
			} else {
				MaterialParameter m = set.cast(MaterialParameter.class);
				if ( m == null ) fizzle(set.asString() + " is not a valid arguement for hold");
				pent.setItemInHand(new ItemStack(m.asMaterial(ctx), 1));
			}
		}
		
		return Parameter.from(pent.getItemInHand());
	}

	@Operation(aliases={"hp"})
	public static Parameter healthOperation(org.bukkit.entity.LivingEntity le, Context ctx, IntegerParameter set) {
		if (set != null) le.setHealth(set.asInteger(ctx));
		return Parameter.from(le.getHealth());
	}
	
	public static Parameter targetOperation(org.bukkit.entity.LivingEntity lent, Context ctx, EntityParameter target) {
		if (!(lent instanceof Creature)) fizzle("Entity needs to be a Creature.");
		Creature c = (Creature) lent;
		if ( target != null ) {
			LivingEntity le = target.asLivingEntity(ctx);
			if ( le == null ) fizzle("Target entity must be a living entity.");
			c.setTarget(le);
		}
		return Parameter.from(c.getTarget());
	}

	


	
}
