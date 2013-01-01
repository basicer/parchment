package com.basicer.parchment.base;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;
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

public class Item extends Spell  {


	@Override
	public String[] getArguments() { return new String[] { "operation?", "args" }; }
	
	public Parameter affect(ItemParameter target, Context ctx) {
		ItemStack itm = target.asItemStack();
		if ( itm == null ) fizzleTarget("Not an item.");
		
		ArrayList<Parameter> args = ctx.getArgs();
		
		ItemMeta m = itm.getItemMeta();
		Parameter pop = ctx.get("operation");
		
		if ( pop == null ) return Parameter.from(itm);
		String op = pop.asString(ctx);
		
		if ( op.equals("name") ) {
			if ( ctx.hasArgs() ) {
				m.setDisplayName(getArgOrFizzle(ctx, 0, StringParameter.class).asString());
				itm.setItemMeta(m);
			}
			String name = m.getDisplayName();
			if ( name == null ) return null;
			return Parameter.from(name);
		} else if ( op.endsWith("lore") ) {
			if ( ctx.hasArgs() ) {
				ArrayList<String> nlore = new ArrayList<String>();
				for ( Parameter p : args ) {
					nlore.add(p.asString());
				}
				m.setLore(nlore);
				itm.setItemMeta(m);
			}
			ArrayList<Parameter> lout = new ArrayList<Parameter>();
			for ( String s : m.getLore() ) {
				lout.add(Parameter.from(s));
			}
			
			return Parameter.createList(lout.toArray(new Parameter[0]));
		} else if ( op.equals("amount") || op.equals("ammount") || op.equals("amt") ) {
			return ammountOperation(itm, ctx);
		} else if ( op.equals("more") || op.equals("max") ) {
			itm.setAmount(itm.getMaxStackSize());
			return Parameter.from(itm.getAmount());
		} else if ( op.equals("damage") || op.equals("dmg") || op.equals("durability") ) {
			if ( ctx.hasArgs() ) {
				itm.setDurability((short)getArgOrFizzle(ctx, 0, IntegerParameter.class).asInteger().intValue());
			}
			return Parameter.from(itm.getDurability());			
		} else if ( op.equals("repair") || op.equals("fix") ) {
			itm.setDurability((short)0);
			return Parameter.from(0);
		} else if ( op.equals("material") || op.equals("type") ) {
			Material old = itm.getType();
			if ( ctx.hasArgs() ) {
				Material now = getArgOrFizzle(ctx, 0, MaterialParameter.class).asMaterial();
				itm.setType(now);
			}
			return Parameter.from(itm.getType());
		} else if ( op.equals("enchant" ) || op.equals("ench") ) {
			return enchantOperation(itm, ctx);
		} else if ( op.equals("bind") ) {
			if ( ctx.hasArgs() ) {
				String str = getArgOrFizzle(ctx, 0, StringParameter.class).asString();
				Book.setSpell(itm, str);
			}
			return Parameter.from(Book.readSpell(itm));
		}
		
		
		fizzle("Invalid operation " + op);
		return null; //Never executed.
 	}
	
	public Parameter ammountOperation(ItemStack itm, Context ctx) {
		if ( ctx.hasArgs() ) {
			itm.setAmount(getArgOrFizzle(ctx, 0, IntegerParameter.class).asInteger());
		}
		return Parameter.from(itm.getAmount());
	}

	public Parameter enchantOperation(ItemStack itm, Context ctx) {
		if ( ctx.hasArgs() ) {
			String name = getArgOrFizzle(ctx, 0, StringParameter.class).asString();
			Enchantment enc = ParseEnchantment(name);
			
			if ( enc == null ) fizzle("Unknwon enchantment: " + name);
			
			Parameter level = getArg(ctx, 1, StringParameter.class);
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
