package com.basicer.parchment.base;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.bukkit.BindingUtils;
import com.basicer.parchment.craftbukkit.Book;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.LivingEntityParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.unsafe.ParchmentNBTBase;
import com.basicer.parchment.unsafe.ParchmentNBTTagCompound;
import com.basicer.parchment.unsafe.ParchmentNBTTagCompoundImpl;
import com.basicer.parchment.unsafe.ParchmentNBTTagList;
import com.basicer.parchment.unsafe.ProxyFactory;


public class Item extends OperationalSpell<ItemParameter>  {


	@Override
	public String[] getAliases() { return new String[] {"i"}; }
	
	public Parameter affect(ItemParameter target, Context ctx) {
		return super.doaffect(target, ctx);
	}
	
	public static ItemStack create(Context ctx, MaterialParameter type) {
		Material mtype = Material.AIR;
		if ( type != null ) mtype = type.asMaterial(ctx);
		org.bukkit.inventory.ItemStack isc = new org.bukkit.inventory.ItemStack(mtype);
		return isc;
		
	}
	
	@Operation(desc = "Designate some spell to handel events generated by target item(s).  Returns current binding.")
	public static Parameter bindOperation(ItemStack itm, Context ctx, StringParameter bind) {

		if ( bind != null ) {
			BindingUtils.setBinding(itm, bind.asString(ctx));
		}
		String bound = BindingUtils.getBinding(itm);
		return Parameter.from(bound == null ? "null" : bound);
	}
	
			
	@Operation(aliases = {"ammount","amt"}, desc = "If given an argument, set the number of items in the target stack.  Returns current ammount.")
	public static Parameter amountOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		if ( amnt != null ) {
			itm.setAmount(amnt.asInteger());
		}
		return Parameter.from(itm.getAmount());
	}
	
	@Operation(aliases = {"dmg","data","durability"}, desc = "If given an argument, set the durability of the item.  Returns the current durability.")
	public static Parameter damageOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		if ( dmg != null ) {
			itm.setDurability((short)((int)dmg.asInteger()));
		}
		return Parameter.from(itm.getDurability());
	}
	
	@Operation(aliases = {"repair"}, desc = "Fully repair target item by setting its durability to 0.")
	public static Parameter fixOperation(ItemStack itm, Context ctx) {
		itm.setDurability((short)0);
		return Parameter.from(itm.getDurability());
	}
	
	public static Parameter loreOperation(ItemStack itm, Context ctx, StringParameter lore) {
		ItemMeta m = itm.getItemMeta();
		if ( lore != null ) {
			String[] lores = lore.asString().split("\r?\n");
			ArrayList<String> lout = new ArrayList<String>();
			for ( String s :lores ) {
				lout.add(s);
			}
			m.setLore(lout);
			itm.setItemMeta(m);
		}
		
		StringBuilder b = new StringBuilder();
		for ( String s : m.getLore() ) {
			if ( b.length() > 0 ) b.append("\n");
			b.append(s);
		}
		return Parameter.from(b.toString());
		
	}
	
	@Operation(aliases = {"type"}, desc = "If given an argument, set the type of the target item.  Returns the current type." )
	public static Parameter materialOperation(ItemStack itm, Context ctx, MaterialParameter mat) {
		if ( mat != null ) {
			itm.setType(mat.as(Material.class));
		}
		return Parameter.from(itm.getType());
	}
	

	@Operation(aliases = {"max"}, desc = "Set the ammount of items in the target stack to the maximum value.")
	public static Parameter moreOperation(ItemStack itm, Context ctx) {
		itm.setAmount(itm.getMaxStackSize());
		return Parameter.from(itm.getAmount());
	}
	
	@Operation(desc = "If given an argument, set the display name of the target item(s).  Returns the current name." )
	public static Parameter nameOperation(ItemStack itm, Context ctx, StringParameter name) {
/*
		ItemMeta m = itm.getItemMeta();
		if ( name != null ) {
			m.setDisplayName(name.asString());
			itm.setItemMeta(m);
		}
		return Parameter.from(m.getDisplayName());
*/
		if ( name != null ) BindingUtils.setItemName(itm, name.asString(ctx));
		return Parameter.from(BindingUtils.getItemName(itm));
		
	}
	
	private static LocationParameter resolveWhere(Parameter where, Context ctx) {
		LocationParameter loc = where.cast(LocationParameter.class, ctx);
		where.cast(LocationParameter.class, ctx);
		if ( loc == null ) {
			PlayerParameter p = where.cast(PlayerParameter.class, ctx);
			if ( p != null ) {
				loc = p.cast(LocationParameter.class, ctx);
				if ( loc != null ) {
					loc = (LocationParameter)Parameter.from(loc.asLocation(ctx).add(0, 2, 0));
				}
			}
		}
		
		 
		if ( loc == null ) fizzle("Couldnt convert " + where.asString() + " to location");
		return loc;
	}
	
	@Operation(desc = "Drop the target item(s) into the world at the given location.  Returns the resulting entity." )
	public static Parameter dropOperation(ItemStack itm, Context ctx, Parameter where) {
		LocationParameter loc = resolveWhere(where, ctx);
		org.bukkit.entity.Entity ent = loc.asWorld(ctx).dropItemNaturally(loc.asLocation(ctx), itm);
		
		
		return Parameter.from(ent);
	}
	
	@Operation(desc = "Place the target item(s) into the world at the given location.  Returns the resulting entity." )
	public static Parameter placeOperation(ItemStack itm, Context ctx, Parameter where) {
		LocationParameter loc = resolveWhere(where, ctx);
		org.bukkit.entity.Entity ent = loc.asWorld(ctx).dropItem(loc.asLocation(ctx), itm);
		ent.setVelocity(new Vector(0,0 ,0));
		ent.teleport(loc.asLocation(ctx), TeleportCause.PLUGIN);
		return Parameter.from(ent);
	}
	
	protected static Parameter makeEnchantReturnValue(ItemStack itm) {
		Map<Enchantment,Integer> x = itm.getEnchantments();
		Parameter[] aout = new Parameter[x.size()];
		int idx = 0;
		for ( Enchantment e : x.keySet() ) {
			aout[idx++] = Parameter.from(e.getName() + ":" + itm.getEnchantmentLevel(e));
		}
		return Parameter.createList(aout);
	}
	
	@Operation(desc = "Enchant the target item(s) with given enchantment name and level.  If no level is specified, the maximum is used.  Unnatural enchantments will return an error." )
	public static Parameter safeEnchantOperation(ItemStack itm, Context ctx, StringParameter name, Parameter level) {
		if ( name != null ) {
			Enchantment enc = ParseEnchantment(name.asString());
			
			if ( enc == null ) fizzle("Unknwon enchantment: " + name.asString());
			
			if ( level == null ) level = Parameter.from(enc.getStartLevel());
			else if ( level.asString().equals("max") ) level = Parameter.from(enc.getMaxLevel());
			
			if ( level.asInteger() == 0 ) {
				itm.removeEnchantment(enc);
			} else {
				try {
					itm.addEnchantment(enc, level.asInteger());
				} catch ( IllegalArgumentException ex ) {
					fizzle(enc.getName() + " : " + ex.getMessage());
				}
			}
		}
		
		return makeEnchantReturnValue(itm);

	}
	
	@Operation(desc = "Enchant the target item(s) with given enchantment name and level.  If no level is specified, the maximum is used.  Unnatural enchantments are okay." )
	public static Parameter enchantOperation(ItemStack itm, Context ctx, StringParameter name, Parameter level) {
		if ( name != null ) {
			Enchantment enc = ParseEnchantment(name.asString());
			
			if ( enc == null ) fizzle("Unknown enchantment: " + name.asString());
			
			if ( level == null ) level = Parameter.from(enc.getStartLevel());
			else if ( level.asString().equals("max") ) level = Parameter.from(enc.getMaxLevel());
			else if ( level.asString().equals("remove") ) level = Parameter.from(0);
			
			if ( level.asInteger() == 0 ) {
				itm.removeEnchantment(enc);
			} else {
				itm.addUnsafeEnchantment(enc, level.asInteger());
			}
		}
		
		return makeEnchantReturnValue(itm);

	}
	
	@Operation(desc = "Put the target item(s) into the given players inventory.  Defaults to caster if no player given." )
	public static Parameter giveOperation(ItemStack itm, Context ctx, PlayerParameter to) {
		if ( to == null ) to = ctx.getCaster().cast(PlayerParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		to.asPlayer(ctx).getInventory().addItem(itm);
		return Parameter.from(itm);
	}
	
	@Operation(desc = "Offer a **copy** of target item(s) to given player by showing them the chest UI." )
	public static Parameter offerOperation(ItemStack itm, Context ctx, PlayerParameter to) {
		if ( to == null ) to = ctx.getCaster().cast(PlayerParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		Player p = to.asPlayer(ctx);
		Inventory i = Bukkit.createInventory(p, 9, "Offer");
		i.addItem(itm);
		p.openInventory(i);
		
		
		return Parameter.from(itm);
	}
	
	@Operation(desc = "Put a **copy** of target itemstack into a player's inventory at in the given slot." )
	public static Parameter forceInvOperation(ItemStack itm, Context ctx, PlayerParameter to, IntegerParameter slot) {
		if ( to == null ) to = ctx.getCaster().cast(PlayerParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		Inventory i = to.asPlayer(ctx).getInventory();
		i.setItem(slot.asInteger(), itm);
		ItemStack io = i.getItem(slot.asInteger());
		return Parameter.from(io);
	}
	
	@Operation(desc = "Equip a **copy** of target itemstack onto a player in its natural slot." )
	public static Parameter equipOperation(ItemStack itm, Context ctx, Parameter to) {
		if ( to == null ) to = ctx.getCaster().cast(LivingEntityParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		
		
		
		LivingEntity p = null;
		if ( to instanceof LivingEntityParameter ) {
			p = ((LivingEntityParameter)to).asLivingEntity(ctx);
		} else if ( to instanceof StringParameter ) {
			Player plr = to.cast(PlayerParameter.class, ctx).asPlayer(ctx);
			if ( plr == null ) fizzle("Equip must be a LivingEntity or player");
			p = plr;
		} else {
			fizzle("Equip must be a LivingEntity or player");
		}

		equipNaturally(p, itm);

		
		return Parameter.from(itm);
	}

	public static void equipNaturally(LivingEntity p, ItemStack itm) {
		switch ( itm.getType() ) {
			case DIAMOND_CHESTPLATE:
			case GOLD_CHESTPLATE:
			case IRON_CHESTPLATE:
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
				p.getEquipment().setChestplate(itm);
				break;
			case DIAMOND_HELMET:
			case GOLD_HELMET:
			case IRON_HELMET:
			case LEATHER_HELMET:
			case CHAINMAIL_HELMET:
			case PUMPKIN:
				p.getEquipment().setHelmet(itm);
				break;
			case DIAMOND_BOOTS:
			case GOLD_BOOTS:
			case IRON_BOOTS:
			case LEATHER_BOOTS:
			case CHAINMAIL_BOOTS:
				p.getEquipment().setBoots(itm);
				break;
			case DIAMOND_LEGGINGS:
			case GOLD_LEGGINGS:
			case IRON_LEGGINGS:
			case LEATHER_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
				p.getEquipment().setLeggings(itm);
				break;
			default:
				//fizzle("Dont know how to equip " + itm.getType());
				p.getEquipment().setItemInHand(itm);
		}
	}
	
	@Operation(desc = "Equip a **copy** of target itemstack onto a player in the specified slot." )
	public static Parameter equipexOperation(ItemStack itm, Context ctx, PlayerParameter to, StringParameter p ) {
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		String ps = p.asString();
		if ( ps.equalsIgnoreCase("helmet") || ps.equalsIgnoreCase("helm") || ps.equalsIgnoreCase("head") ) {
			to.asPlayer(ctx).getEquipment().setHelmet(itm);
		} else if ( ps.equalsIgnoreCase("chest") || ps.equalsIgnoreCase("chestplate") ) {
			to.asPlayer(ctx).getEquipment().setChestplate(itm);
		} else if ( ps.equalsIgnoreCase("boots") || ps.equalsIgnoreCase("feet") ) {
			to.asPlayer(ctx).getEquipment().setBoots(itm);
		} else if ( ps.equalsIgnoreCase("pants") ||  ps.equalsIgnoreCase("leggings") ) {
			to.asPlayer(ctx).getEquipment().setLeggings(itm);
		} else {
			ctx.sendDebugMessage("Where is the " + ps);
		}
		return Parameter.from(itm);
		
	}
	
	public static Parameter testOperation(ItemStack itm, Context ctx, StringParameter path) {
		try {
			ctx.sendDebugMessage("Starting...");
			ParchmentNBTBase tag = ParchmentNBTTagCompoundImpl.getTag(itm, false);
			String spath = path.asString();
			ctx.sendDebugMessage("1 " +spath);
			
						
			String[] pth = spath.split("\\.");
			for ( int i = 0; i < pth.length; ++i) {
				if ( tag instanceof ParchmentNBTTagCompound ) {
					ParchmentNBTTagCompound tagc = (ParchmentNBTTagCompound) tag;
				 	tag = tagc.get(pth[i]);
				 	
				} else if ( tag instanceof ParchmentNBTTagList ) { 
					ParchmentNBTTagList tagl = (ParchmentNBTTagList) tag;
					tag = tagl.get(Integer.parseInt(pth[i]));

				} else {
					ctx.sendDebugMessage("Invalid path");
					return Parameter.from(false);
				}
				
				if ( tag.getTypeId() == 10 ) {
					tag = ProxyFactory.createProxy(ParchmentNBTTagCompound.class, tag.unproxy());
				} else if ( tag.getTypeId() == 9 ) {
					tag = ProxyFactory.createProxy(ParchmentNBTTagList.class, tag.unproxy());
				}
			}

			
			try {
				Object data = tag.unproxy().getClass().getField("data").get(tag.unproxy());
				return Parameter.fromObject(data);
			} catch (NoSuchFieldException ex) {
				return Parameter.from(tag.toString());
			}
			//net.minecraft.server.v1_4_6.NBTTagCompound x = new net.minecraft.server.v1_4_6.NBTTagCompound();
			
			
		} catch ( UndeclaredThrowableException ex ) {
			throw new RuntimeException(ex.getCause());
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex.getCause());
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex.getCause());
		} catch (SecurityException ex) {
			throw new RuntimeException(ex.getCause());
		}
		
	}
	
	public static Enchantment ParseEnchantment(String name) {
		name = name.toUpperCase();
		Enchantment enc = Enchantment.getByName(name);
		if ( enc != null ) return enc;
		try {
			int x = Integer.parseInt(name);
			enc = Enchantment.getById(x);
			if ( enc != null ) return enc;
		} catch ( NumberFormatException ex ) {
			
		}
		
		if ( name.equals("POWER") ) return Enchantment.ARROW_DAMAGE;
		if ( name.equals("FORTUNE") ) return Enchantment.LOOT_BONUS_BLOCKS;
		if ( name.equals("LOOTING") ) return Enchantment.LOOT_BONUS_MOBS;
		if ( name.equals("INFINITE") ) return Enchantment.ARROW_INFINITE;
		if ( name.equals("INFINITY") ) return Enchantment.ARROW_INFINITE;
		if ( name.equals("UNBREAKING") ) return Enchantment.DURABILITY;
		if ( name.equals("SILKTOUCH") ) return Enchantment.SILK_TOUCH;
		if ( name.equals("PUNCH") ) return Enchantment.ARROW_KNOCKBACK;
		if ( name.equals("SHARPNESS") ) return Enchantment.DAMAGE_ALL;
		if ( name.equals("FLAME") ) return Enchantment.ARROW_FIRE;
		if ( name.equals("FIRE") ) return Enchantment.FIRE_ASPECT;
		if ( name.equals("SMITE")) return Enchantment.DAMAGE_UNDEAD;
		if ( name.equals("BANE")) return Enchantment.DAMAGE_ARTHROPODS;
		
		if ( name.equals("FAST")) return Enchantment.DIG_SPEED;
		return null;
	}
	
}
