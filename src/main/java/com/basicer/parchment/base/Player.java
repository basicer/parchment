package com.basicer.parchment.base;


import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.bukkit.ParchmentPlugin;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalTargetedCommand;

import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;
import org.bukkit.scoreboard.*;

public class Player extends OperationalTargetedCommand<PlayerParameter> {
	
	public static Class<? extends OperationalTargetedCommand<?>> getBaseClass() { return LEntity.class; }

	@Override
	public String[] getAliases() { return new String[] {"p"}; }
	
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


	@Operation(desc ="Empties the players inventory.")
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

	@Operation(desc ="Returns the palyers IP address.")
	public static Parameter ipOperation(org.bukkit.entity.Player pent, Context ctx) {

		return StringParameter.from(pent.getAddress().getAddress().getHostAddress());
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





	@Operation(desc = "Force a player to say something.")
	public static Parameter chatOperation(org.bukkit.entity.Player pent, Context ctx, Parameter text) {
		pent.chat(text.asString(ctx));
		return Parameter.from(pent);
	}

	@Operation(desc = "Get or set a players bed spawn point.")
	public static Parameter bedSpawnOperation(org.bukkit.entity.Player pent, Context ctx, LocationParameter set) {
		if ( set != null ) {
			pent.setBedSpawnLocation(set.asLocation(ctx), true);
		}
		return Parameter.from(pent.getBedSpawnLocation());
	}

	@Operation(aliases = {"fly"})
	public static Parameter flightOperation(org.bukkit.entity.Player pent, Context ctx, BooleanParameter on) {
		if ( on == null ) return Parameter.from(pent.getAllowFlight());
		pent.setAllowFlight(on.asBoolean());
		return Parameter.from(pent.getAllowFlight());
	}

	@Operation(aliases = {"flying"})
	public static Parameter isflyingOperation(org.bukkit.entity.Player pent, Context ctx, BooleanParameter on) {
		if ( on == null ) return Parameter.from(pent.isFlying());
		boolean onb = on.asBoolean();
		if ( onb && !pent.getAllowFlight() ) fizzle("Can't make player fly if flight is not allowed.");
		pent.setFlying(onb);
		return Parameter.from(pent.isFlying());
	}
	
	public static Parameter flySpeedOperation(org.bukkit.entity.Player pent, Context ctx, Parameter v) {
		if ( v != null ) {
			if ( v.asString(ctx).equals("default") ) pent.setFlySpeed(0.1f);
			else pent.setFlySpeed(v.asDouble().floatValue());
		}
		return Parameter.from(pent.getFlySpeed());
	}

	public static Parameter walkSpeedOperation(org.bukkit.entity.Player pent, Context ctx, Parameter v) {
		if ( v != null ) {
			if ( v.asString(ctx).equals("default") ) pent.setWalkSpeed(0.2f);
			else pent.setWalkSpeed(v.asDouble().floatValue());
		}
		return Parameter.from(pent.getWalkSpeed());
	}


	public static Parameter texturePackOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v != null ) pent.setTexturePack(v.asString(ctx));
		else fizzle("texturepack requires a texture pack to set.");
		return Parameter.EmptyString;
	}
	
	public static Parameter listnameOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v != null ) pent.setPlayerListName(v.asString());
		return Parameter.from(pent.getPlayerListName());
	}



	public static Parameter giveOperation(org.bukkit.entity.Player pent, Context ctx, Parameter set) {

		ItemStack is = null;
		if ( set != null ) {
			if ( set instanceof ItemParameter) {
				is = ((ItemParameter) set).asItemStack(ctx);
			} else if ( set instanceof MaterialParameter ) {
				is = new ItemStack(((MaterialParameter) set).asMaterial(ctx), 1);
			} else {
				is = Item.createItemstackFromString(set.asString(ctx));
			}
		}

		if ( is == null ) fizzle("Couldn't convert give input to an item");
		pent.getInventory().addItem(is);
		return PlayerParameter.from(pent);
	}

	
	public static Parameter opOperation(org.bukkit.entity.Player pent, Context ctx, BooleanParameter v) {
		if ( v != null ) pent.setOp(v.asBoolean(ctx));
		return Parameter.from(pent.isOp() ? 1 : 0);
	}

	@Operation(aliases = {"hasPerm"})
	public static Parameter hasPermissionOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v == null ) fizzle("What permission where you lookin for ?");
		return Parameter.from(pent.hasPermission(v.asString(ctx)));
	}

	@Operation(aliases = {"giveperm"})
	public static Parameter givePermissionOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v == null ) fizzle("What permission where you lookin for ?");
		PermissionAttachment target = null;
		for ( PermissionAttachmentInfo pi : pent.getEffectivePermissions() ) {
			if ( pi.getPermissible() == pent && pi.getAttachment() != null && pi.getAttachment().getPlugin() == ParchmentPluginLite.instance() ) {
				target = pi.getAttachment();
				break;
			}
		}
		if ( target == null ) target = new PermissionAttachment(ParchmentPluginLite.instance(), pent);
		target.setPermission(v.asString(ctx), true);
		return Parameter.from(pent.hasPermission(v.asString(ctx)));
	}

	@Operation(aliases = {"takeperm", "delperm"})
	public static Parameter removePermissionOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter v) {
		if ( v == null ) fizzle("What permission where you lookin for ?");
		PermissionAttachment target = null;
		for ( PermissionAttachmentInfo pi : pent.getEffectivePermissions() ) {
			if ( pi.getPermissible() == pent.getPlayer() && pi.getAttachment().getPlugin() == ParchmentPluginLite.instance() ) {
				target = pi.getAttachment();
				break;
			}
		}
		if ( target == null ) target = new PermissionAttachment(ParchmentPluginLite.instance(), pent);
		target.setPermission(v.asString(ctx), false);
		return Parameter.from(pent.hasPermission(v.asString(ctx)));
	}

	@Operation(aliases = {"perms"})
	public static Parameter permissionsOperation(org.bukkit.entity.Player pent, Context ctx) {
		ArrayList<Parameter> list = new ArrayList<Parameter>();
		for ( PermissionAttachmentInfo i : pent.getEffectivePermissions() ) {
			list.add(Parameter.from(i.getPermission()));
		}
		return ListParameter.from(list);
	}

	@Operation(aliases={"gm"})
	public static Parameter gameModeOperation(org.bukkit.entity.Player pent, Context ctx, Parameter set) {
		if ( set != null ) {
			GameMode m = set.asEnum(GameMode.class);
			if ( m == null ) fizzle(set.asString() + " is not a valid arguement for hold");
			pent.setGameMode(m);
		}
		
		return Parameter.from(pent.getGameMode().name());
	}
	
	public static Parameter closeInventoryOperation(org.bukkit.entity.Player pent, Context ctx) {
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

	@Operation(desc = "Returns the amount of money a player has from vault.", requires = {"vault"})
	public static Parameter moneyOperation(org.bukkit.entity.Player pent, Context ctx) {
		return  Parameter.from(((Economy) getEconomyOrFizzle()).getBalance(pent));
	}

	@Operation(requires = {"vault"})
	public static Parameter giveMoneyOperation(org.bukkit.entity.Player pent, Context ctx, DoubleParameter amount) {
		return  Parameter.from(((Economy) getEconomyOrFizzle()).depositPlayer(pent, amount.asDouble(ctx)).balance);
	}

	@Operation(requires = {"vault"})
	public static Parameter takeMoneyOperation(org.bukkit.entity.Player pent, Context ctx, DoubleParameter amount) {
		return  Parameter.from(((Economy)getEconomyOrFizzle()).withdrawPlayer(pent, amount.asDouble(ctx)).balance);
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
		 public static Parameter giveExpOperation(org.bukkit.entity.Player pent, Context ctx, IntegerParameter amount) {
		if ( amount != null ) pent.giveExp(amount.asInteger());
		return Parameter.from(pent.getTotalExperience());
	}


	public static Parameter giveLevelOperation(org.bukkit.entity.Player pent, Context ctx, IntegerParameter amount) {
		if ( amount != null ) pent.giveExpLevels(amount.asInteger(ctx));
		return Parameter.from(pent.getLevel());
	}

	private static  Scoreboard getScoreBaord(org.bukkit.entity.Player pent, Context ctx) {
		if ( pent.getScoreboard() != null ) return pent.getScoreboard();
		return Bukkit.getScoreboardManager().getMainScoreboard();
	}

	public static Parameter teamOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter team) {
		Scoreboard sb = getScoreBaord(pent, ctx);

		if ( team != null ) {
			Team old = sb.getPlayerTeam(pent);
			if ( old != null ) old.removePlayer(pent);
			Team nue = sb.getTeam(team.asString());
			if ( nue == null ) fizzle("No such team: " + team.asString());
			nue.addPlayer(pent);
		}
		return Parameter.from(sb.getPlayerTeam(pent).getName());
	}

	public static Parameter scoreOperation(org.bukkit.entity.Player pent, Context ctx, StringParameter objective, IntegerParameter value) {
		Scoreboard sb =  getScoreBaord(pent, ctx);
		Objective o = sb.getObjective(objective.asString());
		if ( o == null ) fizzle("No such objective" + objective.asString(ctx));
		for ( Score s : sb.getScores(pent) ) {
			if ( s.getObjective() != o ) continue;
			if ( value != null ) s.setScore(value.asInteger(ctx));
			return Parameter.from(s.getScore());
		}

		fizzle("Coudln't find that player/score pair.");
		return null; //Unreached
	}

	public static Parameter scoreboardOperation(org.bukkit.entity.Player pent, Context ctx, OpaqueParameter<Scoreboard> value) {
		if ( value != null ) pent.setScoreboard(value.getValue());
			return OpaqueParameter.create(pent.getScoreboard());
	}

	@Operation(desc = "Restore target player's hunger, saturation, and exaustion.")
	public static Parameter feedOperation(org.bukkit.entity.Player pent, Context ctx) {
		pent.setFoodLevel(20);
		pent.setSaturation(10);
		pent.setExhaustion(0.0f);
		return Parameter.from(pent);
	}


	@Operation(desc = "Displays text to that player.")
	public static Parameter putsOperation(org.bukkit.entity.Player pent, Context ctx, Parameter text) {
		pent.sendMessage(text.asString());
		return Parameter.from(pent);
	}
}
