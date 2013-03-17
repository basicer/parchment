package com.basicer.parchment.base;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.NBTBase;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
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


	

	
	
	public Parameter affect(ItemParameter target, Context ctx) {
		return super.doaffect(target, ctx);
	}
	
	public static ItemStack create(Context ctx) {
		org.bukkit.inventory.ItemStack isc = new org.bukkit.inventory.ItemStack(0);
		return isc;
		
	}
	
	public static Parameter bindOperation(ItemStack itm, Context ctx, StringParameter bind) {

		if ( bind != null ) {			
			Book.setSpell(itm, bind.asString());
		}
		String bound = Book.readSpell(itm);
		return Parameter.from(bound == null ? "null" : bound);
	}
	
	public static Parameter ammountOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		return amountOperation(itm, ctx, amnt);
	}
	
	public static Parameter amtOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		return amountOperation(itm, ctx, amnt);
	}
		
	public static Parameter amountOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		if ( amnt != null ) {
			itm.setAmount(amnt.asInteger());
		}
		return Parameter.from(itm.getAmount());
	}
	
	public static Parameter damageOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		if ( dmg != null ) {
			itm.setDurability((short)((int)dmg.asInteger()));
		}
		return Parameter.from(itm.getDurability());
	}
	
	public static Parameter dmgOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		return damageOperation(itm, ctx, dmg);
	}
	
	public static Parameter dataOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		return damageOperation(itm, ctx, dmg);
	}
	
	public static Parameter durabilityOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		return damageOperation(itm, ctx, dmg);
	}
	
	
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
	
	public static Parameter repairOperation(ItemStack itm, Context ctx) {
		return fixOperation(itm, ctx);
	}
	
	public static Parameter materialOperation(ItemStack itm, Context ctx, MaterialParameter mat) {
		if ( mat != null ) {
			itm.setType(mat.as(Material.class));
		}
		return Parameter.from(itm.getType());
	}
	
	public static Parameter typeOperation(ItemStack itm, Context ctx, MaterialParameter mat) {
		if ( mat != null ) {
			itm.setType(mat.as(Material.class));
		}
		return Parameter.from(itm.getType());
	}
	
	public static Parameter maxOperation(ItemStack itm, Context ctx) {
		return moreOperation(itm, ctx);
	}
	
	public static Parameter moreOperation(ItemStack itm, Context ctx) {
		itm.setAmount(itm.getMaxStackSize());
		return Parameter.from(itm.getAmount());
	}
	
	public static Parameter nameOperation(ItemStack itm, Context ctx, StringParameter name) {
		ItemMeta m = itm.getItemMeta();
		if ( name != null ) {
			m.setDisplayName(name.asString());
			itm.setItemMeta(m);
		}
		return Parameter.from(m.getDisplayName());
	}
	
	public static Parameter dropOperation(ItemStack itm, Context ctx, Parameter where) {
		LocationParameter loc = where.cast(LocationParameter.class, ctx);
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
		ctx.getWorld().dropItem(loc.asLocation(ctx), itm);
		
		
		return loc;
	}
	
	protected static Parameter makeEnchantResturnValue(ItemStack itm) {
		Map<Enchantment,Integer> x = itm.getEnchantments();
		Parameter[] aout = new Parameter[x.size()];
		int idx = 0;
		for ( Enchantment e : x.keySet() ) {
			aout[idx++] = Parameter.from(e.getName() + ":" + itm.getEnchantmentLevel(e));
		}
		return Parameter.createList(aout);
	}
	
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
		
		return makeEnchantResturnValue(itm);

	}
	
	public static Parameter enchantOperation(ItemStack itm, Context ctx, StringParameter name, Parameter level) {
		if ( name != null ) {
			Enchantment enc = ParseEnchantment(name.asString());
			
			if ( enc == null ) fizzle("Unknwon enchantment: " + name.asString());
			
			if ( level == null ) level = Parameter.from(enc.getStartLevel());
			else if ( level.asString().equals("max") ) level = Parameter.from(enc.getMaxLevel());
			else if ( level.asString().equals("remove") ) level = Parameter.from(0);
			
			if ( level.asInteger() == 0 ) {
				itm.removeEnchantment(enc);
			} else {
				itm.addUnsafeEnchantment(enc, level.asInteger());
			}
		}
		
		return makeEnchantResturnValue(itm);

	}
	
	public static Parameter giveOperation(ItemStack itm, Context ctx, PlayerParameter to) {
		if ( to == null ) to = ctx.getCaster().cast(PlayerParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		to.asPlayer(ctx).getInventory().addItem(itm);
		return Parameter.from(itm);
	}
	
	public static Parameter forceInvOperation(ItemStack itm, Context ctx, PlayerParameter to, IntegerParameter slot) {
		if ( to == null ) to = ctx.getCaster().cast(PlayerParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		Inventory i = to.asPlayer(ctx).getInventory();
		i.setItem(slot.asInteger(), itm);
		ItemStack io = i.getItem(slot.asInteger());
		return Parameter.from(io);
	}
	
	public static Parameter equipOperation(ItemStack itm, Context ctx, PlayerParameter to) {
		if ( to == null ) to = ctx.getCaster().cast(PlayerParameter.class);
		if ( to == null ) fizzle("You must pick someone to give the item to.");
		
		Player p = to.asPlayer(ctx);
		
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
				fizzle("Dont know how to equip " + itm.getType());
		}
		
		return Parameter.from(itm);
	}
	
	public static Parameter equipExOperation(ItemStack itm, Context ctx, PlayerParameter to, StringParameter p ) {
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
		
		return null;
	}
	
}
