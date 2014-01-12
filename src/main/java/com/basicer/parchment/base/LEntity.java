package com.basicer.parchment.base;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.basicer.parchment.annotations.Operation;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
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
		
		fizzle("No entities found there");
		return null;
	}

	public Parameter affect(BlockParameter target, Context ctx) {
		return affect(target.cast(LocationParameter.class), ctx);
	}


	public static org.bukkit.entity.LivingEntity create(Context ctx, StringParameter type, LocationParameter where) {
		//where.asWorld(ctx).spawn(where.asLocation(ctx), Class.forName(type));
		Location l = where.asLocation(ctx);
		org.bukkit.entity.EntityType etype = type.asEnum(org.bukkit.entity.EntityType.class);

		if ( etype == null ) fizzle("No such entity type: " + type.asString(ctx));
		if ( !etype.isSpawnable() ) fizzle("Entity type is not spawnable.");
		if ( !etype.isAlive() ) fizzle("Must choose living entity type.");
		try {
			return (LivingEntity)l.getWorld().spawnEntity(l, etype);
		} catch ( IllegalArgumentException ex ) {
			fizzle(ex.getMessage());
			return null;
		}


	}


	private static Pattern potion_match = Pattern.compile("^([a-zA-Z]+)([0-9]+)?$");

	public static PotionEffectType parsePotionEffect(String name) {
		switch ( name.toUpperCase() ) {
			case "STR":
			case "STRENGTH":
			case "DAMAGE":
			case "DMG":
				return PotionEffectType.INCREASE_DAMAGE;
			case "HASTE":
				return PotionEffectType.FAST_DIGGING;
			default:
				return PotionEffectType.getByName(name.toUpperCase());
		}




	}

	@Operation(aliases={"apot"})
	public static Parameter addpotionOperation(org.bukkit.entity.LivingEntity lent, Context ctx, StringParameter name, DoubleParameter dur)
	{

		if ( name == null ) fizzle("must specify potion name");
		Matcher m = potion_match.matcher(name.asString(ctx));
		if ( !m.matches() ) fizzle("Invalid potion effect name");
		int amp = 0;
		if ( m.group(2) != null ) {
			amp = Integer.decode(m.group(2)) - 1;
		}

		PotionEffectType eff = parsePotionEffect(m.group(1));
		double durd = 120;
		if ( dur != null ) durd = dur.asDouble(ctx);
		lent.addPotionEffect(eff.createEffect((int)(durd * 20), amp));


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
	public static Parameter healthOperation(org.bukkit.entity.LivingEntity le, Context ctx, DoubleParameter set) {
		if (set != null) le.setHealth(set.asDouble(ctx));
		return Parameter.from(le.getHealth());
	}

	@Operation(aliases={"fhp"}, desc="Set's entities HP, raising its max hp if nessessiary.")
	public static Parameter forceHealthOperation(org.bukkit.entity.LivingEntity le, Context ctx, DoubleParameter set) {

		if (set != null) {
			double nhp = set.asDouble(ctx);
			if ( le.getMaxHealth() < nhp ) le.setMaxHealth(nhp);
			le.setHealth(nhp);
		}
		return Parameter.from(le.getHealth());
	}

	@Operation(aliases={"maxhp"})
	public static Parameter maxHealthOperation(org.bukkit.entity.LivingEntity le, Context ctx, DoubleParameter set) {
		if (set != null) le.setMaxHealth(set.asDouble(ctx));
		return Parameter.from(le.getMaxHealth());
	}

	@Operation()
	public static Parameter killOperation(org.bukkit.entity.LivingEntity le, Context ctx) {
		le.damage(le.getHealth());
		return Parameter.from(le);
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

	public static Parameter equipOperation(org.bukkit.entity.LivingEntity lent, Context ctx, Parameter what) {
		if ( what instanceof  ItemParameter ) {
			Item.equipNaturally(lent, ((ItemParameter) what).asItemStack(ctx));
			return Parameter.from(lent);
		}

		Material mat = what.cast(MaterialParameter.class, ctx).asMaterial(ctx);
		if ( mat == null ) fizzle(what.asString() + " is not a material.");
		org.bukkit.inventory.ItemStack isc = new org.bukkit.inventory.ItemStack(mat);
		Item.equipNaturally(lent, isc);

		return Parameter.from(lent);

	}

	@Operation(argnames = {"amount", "fromEntity"}, desc = "Deals `amount` damage to this entity.", aliases = {"hurt"})
	public static Parameter damageOperation(org.bukkit.entity.LivingEntity lent, Context ctx, DoubleParameter amount, LivingEntityParameter oent) {
		double amountd = amount.asDouble();
		if ( oent != null ) {
			lent.damage(amountd, oent.asLivingEntity(ctx));
		} else {
			lent.damage(amountd);
		}

		return LivingEntityParameter.from(lent);

	}

	
}
