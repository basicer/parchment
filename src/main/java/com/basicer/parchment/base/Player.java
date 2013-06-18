package com.basicer.parchment.base;


import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.bukkit.ParchmentPlugin;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;

import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;

public class Player extends OperationalSpell<PlayerParameter> {
	
	public static Class<? extends OperationalSpell<?>> getBaseClass() { return LEntity.class; }
	
	
	
	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
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
	
	public static Parameter inventorylistOperation(org.bukkit.entity.Player pent, Context ctx) {
		Inventory inv = pent.getInventory();
		ArrayList<Parameter> out = new ArrayList<Parameter>();
		for ( ItemStack i : inv.getContents() ) {
			out.add(Parameter.from(i));
		}
		
		return ListParameter.from(out);
		
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
	
	
	@Operation(aliases = {"fly"})
	public static Parameter flightOperation(org.bukkit.entity.Player pent, Context ctx, BooleanParameter on) {
		if ( on == null ) return Parameter.from(pent.getAllowFlight());
		pent.setAllowFlight(on.asBoolean());
		return Parameter.from(pent.getAllowFlight());
	}
	
	public static Parameter flyspeedOperation(org.bukkit.entity.Player pent, Context ctx, DoubleParameter v) {
		if ( v != null ) pent.setFlySpeed(v.asDouble().floatValue());
		return Parameter.from(pent.getFlySpeed());
	}
	
	public static Parameter texturpackOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v != null ) pent.setTexturePack(v.asString(ctx));
		else fizzle("texturepack requires a texture pack to set.");
		return Parameter.EmptyString;
	}
	
	public static Parameter listnameOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v != null ) pent.setPlayerListName(v.asString());
		return Parameter.from(pent.getPlayerListName());
	}

	
	
	public static Parameter opOperation(org.bukkit.entity.Player pent, Context ctx, BooleanParameter v) {
		if ( v != null ) pent.setOp(v.asBoolean(ctx));
		return Parameter.from(pent.isOp() ? 1 : 0);
	}
	
	public static Parameter haspermissionOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v == null ) fizzle("What permission where you lookin for ?");
		return Parameter.from(pent.hasPermission(v.asString(ctx)));
	}
	
	public static Parameter permissionsOperation(org.bukkit.entity.Player pent, Context ctx) {
		ArrayList<Parameter> list = new ArrayList<Parameter>();
		for ( PermissionAttachmentInfo i : pent.getEffectivePermissions() ) {
			list.add(Parameter.from(i.getPermission()));
		}
		return ListParameter.from(list);
	}

	public static Parameter gamemodeOperation(org.bukkit.entity.Player pent, Context ctx, Parameter set) {
		if ( set != null ) {
			GameMode m = set.asEnum(GameMode.class);
			if ( m == null ) fizzle(set.asString() + " is not a valid arguement for hold");
			pent.setGameMode(m);
		}
		
		return Parameter.from(pent.getGameMode().name());
	}
	
	public static Parameter closeinventoryOperation(org.bukkit.entity.Player pent, Context ctx) {
		pent.closeInventory();
		return Parameter.from(pent);
	}

	/* Vault Stuff */

	private static Object getEconomyOrFizzle() {
		Economy e = ParchmentPlugin.getInstance().getVaultEconomy();
		if ( e == null ) fizzle("That operation requires Vault");
		if ( !e.isEnabled() ) fizzle("That operations requires Vault economy");
		return e;
	}

	@Operation(desc = "Returns the amount of money a player has from vault.")
	public static Parameter moneyOperation(org.bukkit.entity.Player pent, Context ctx) {
		return  Parameter.from(((Economy) getEconomyOrFizzle()).getBalance(pent.getName()));
	}

	public static Parameter giveMoneyOperation(org.bukkit.entity.Player pent, Context ctx, DoubleParameter amount) {
		return  Parameter.from(((Economy) getEconomyOrFizzle()).depositPlayer(pent.getName(), amount.asDouble(ctx)).balance);
	}

	public static Parameter takeMoneyOperation(org.bukkit.entity.Player pent, Context ctx, DoubleParameter amount) {
		return  Parameter.from(((Economy)getEconomyOrFizzle()).withdrawPlayer(pent.getName(), amount.asDouble(ctx)).balance);
	}


	public static Parameter levelOperation(org.bukkit.entity.Player pent, Context ctx, IntegerParameter amount) {
		if ( amount != null ) pent.setLevel(amount.asInteger(ctx));
		return Parameter.from(pent.getLevel());
	}

	@Operation(aliases = {"xp", "experience"})
	public static Parameter expOperation(org.bukkit.entity.Player pent, Context ctx, IntegerParameter amount) {
		if ( amount != null ) pent.setTotalExperience(amount.asInteger(ctx).intValue());
		return Parameter.from(pent.getTotalExperience());
	}

	@Operation(aliases = {"givexp"})
		 public static Parameter giveexpOperation(org.bukkit.entity.Player pent, Context ctx, IntegerParameter amount) {
		if ( amount != null ) pent.giveExp(amount.asInteger());
		return Parameter.from(pent.getTotalExperience());
	}


	public static Parameter giveLevelOperation(org.bukkit.entity.Player pent, Context ctx, IntegerParameter amount) {
		if ( amount != null ) pent.giveExpLevels(amount.asInteger(ctx));
		return Parameter.from(pent.getLevel());
	}


	@Operation(desc = "Restore target player's hunger, saturation, and exaustion.")
	public static Parameter feedOperation(org.bukkit.entity.Player pent, Context ctx) {
		pent.setFoodLevel(20);
		pent.setSaturation(10);
		pent.setExhaustion(0.0f);
		return Parameter.from(pent);
	}
	
}
