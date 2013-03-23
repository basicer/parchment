package com.basicer.parchment.base;


import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.Spell.FirstParamaterTargetType;

import com.basicer.parchment.parameters.*;

public class Player extends OperationalSpell<PlayerParameter> {
	
	public Class<? extends OperationalSpell<?>> getBaseClass() { return Entity.class; }
	
	
	
	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.FuzzyMatch;
	}



	public Parameter affect(PlayerParameter target, Context ctx) {
		return this.doaffect(target, ctx);
	}
	
	public Parameter affect(LocationParameter target, Context ctx) {
		for ( org.bukkit.entity.Player e : target.as(Location.class).getWorld().getPlayers() ) {
			if ( e.getLocation().distanceSquared(target.as(Location.class)) < 4 ) {
				return this.doaffect((PlayerParameter)Parameter.from(e), ctx);
			}
		}
		fizzle("No players fond there");
		return null;
	}
	
	public Parameter affect(BlockParameter target, Context ctx) {
		return affect(target.cast(LocationParameter.class), ctx);
	}


	public static Parameter clearOperation(org.bukkit.entity.Player pent, Context ctx) {
		pent.getInventory().clear();
		return Parameter.from(pent);
	}
	
	public static Parameter offerOperation(org.bukkit.entity.Player pent, Context ctx, List<Parameter> args) {
		if ( args == null ) fizzle("You must specify some things to offer.");
		Inventory i = Bukkit.createInventory(pent, args.size() + 9 - (args.size() % 9), "Offer");
		for ( Parameter p : args ) {
			if ( p instanceof ItemParameter ) {
				i.addItem(p.as(ItemStack.class));
			} else {
				MaterialParameter m = p.cast(MaterialParameter.class);
				if ( m != null ) {
					i.addItem(new ItemStack(m.asMaterial(ctx), 1));
				}
			}
		}
		pent.openInventory(i);
		return Parameter.from(pent);
	}
	
	
	public static Parameter flightOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter on) {
		if ( on == null ) return Parameter.from(pent.getAllowFlight());
		pent.setAllowFlight(on.asBoolean());
		return Parameter.from(pent.getAllowFlight());
	}
	
	public static Parameter flyspeedOperation(org.bukkit.entity.Player pent, Context ctx, DoubleParameter v) {
		if ( v != null ) pent.setFlySpeed(v.asDouble().floatValue());
		return Parameter.from(pent.getFlySpeed());
	}
	
}
