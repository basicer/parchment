package com.basicer.parchment.base;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
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
import com.basicer.parchment.parameters.MaterialParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;

public class Item extends OperationalSpell<ItemParameter>  {


	

	
	
	public Parameter affect(ItemParameter target, Context ctx) {
		return super.doaffect(target, ctx);
	}
	
	public Parameter bindOperation(ItemStack itm, Context ctx, StringParameter bind) {
		if ( bind != null ) {
			Book.setSpell(itm, bind.asString());
		}
		return Parameter.from(Book.readSpell(itm));
	}
	
	public Parameter ammountOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		return amountOperation(itm, ctx, amnt);
	}
	
	public Parameter amtOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		return amountOperation(itm, ctx, amnt);
	}
		
	public Parameter amountOperation(ItemStack itm, Context ctx, IntegerParameter amnt) {
		if ( amnt != null ) {
			itm.setAmount(amnt.asInteger());
		}
		return Parameter.from(itm.getAmount());
	}
	
	public Parameter damageOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		if ( dmg != null ) {
			itm.setDurability((short)((int)dmg.asInteger()));
		}
		return Parameter.from(itm.getDurability());
	}
	
	public Parameter dmgOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		return damageOperation(itm, ctx, dmg);
	}
	
	public Parameter durabilityOperation(ItemStack itm, Context ctx, IntegerParameter dmg) {
		return damageOperation(itm, ctx, dmg);
	}
	
	
	public Parameter fixOperation(ItemStack itm, Context ctx) {
		itm.setDurability((short)0);
		return Parameter.from(itm.getDurability());
	}
	
	public Parameter loreOperation(ItemStack itm, Context ctx, StringParameter lore) {
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
	
	public Parameter repairOperation(ItemStack itm, Context ctx) {
		return fixOperation(itm, ctx);
	}
	
	public Parameter materialOperation(ItemStack itm, Context ctx, MaterialParameter mat) {
		if ( mat != null ) {
			itm.setType(mat.asMaterial());
		}
		return Parameter.from(itm.getType());
	}
	
	public Parameter typeOperation(ItemStack itm, Context ctx, MaterialParameter mat) {
		if ( mat != null ) {
			itm.setType(mat.asMaterial());
		}
		return Parameter.from(itm.getType());
	}
	
	public Parameter maxOperation(ItemStack itm, Context ctx) {
		return moreOperation(itm, ctx);
	}
	
	public Parameter moreOperation(ItemStack itm, Context ctx) {
		itm.setAmount(itm.getMaxStackSize());
		return Parameter.from(itm.getAmount());
	}
	
	public Parameter nameOperation(ItemStack itm, Context ctx, StringParameter name) {
		ItemMeta m = itm.getItemMeta();
		if ( name != null ) {
			m.setDisplayName(name.asString());
			itm.setItemMeta(m);
		}
		return Parameter.from(m.getDisplayName());
	}
	
	public Parameter enchantOperation(ItemStack itm, Context ctx, StringParameter name, Parameter level) {
		if ( name != null ) {
			Enchantment enc = ParseEnchantment(name.asString());
			
			if ( enc == null ) fizzle("Unknwon enchantment: " + name.asString());
			
			if ( level == null ) level = Parameter.from(enc.getStartLevel());
			else if ( level.asString().equals("max") ) level = Parameter.from(enc.getMaxLevel());
			
			if ( level.asInteger() == 0 ) {
				itm.removeEnchantment(enc);
			} else {
				itm.addEnchantment(enc, level.asInteger());
			}
		}
		
		Map<Enchantment,Integer> x = itm.getEnchantments();
		Parameter[] aout = new Parameter[x.size()];
		int idx = 0;
		for ( Enchantment e : x.keySet() ) {
			aout[idx++] = Parameter.from(e.getName() + ":" + itm.getEnchantmentLevel(e));
		}
		return Parameter.createList(aout);
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
		if ( name.equals("UNBREAKING") ) return Enchantment.DURABILITY;
		if ( name.equals("SILKTOUCH") ) return Enchantment.SILK_TOUCH;
		if ( name.equals("PUNCH") ) return Enchantment.KNOCKBACK;
		
		return null;
	}
	
}
