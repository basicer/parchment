package com.basicer.parchment.base;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
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
			if ( args.size() > 0 ) {
				m.setDisplayName(getArgOrFizzle(ctx, 0, StringParameter.class).asString());
				itm.setItemMeta(m);
			}
			String name = m.getDisplayName();
			if ( name == null ) return null;
			return Parameter.from(name);
		} else if ( op.endsWith("lore") ) {
			if ( args.size() > 0 ) {
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
			if ( args.size() > 0 ) {
				itm.setAmount(getArgOrFizzle(ctx, 0, IntegerParameter.class).asInteger());
			}
			return Parameter.from(itm.getAmount());
		} else if ( op.equals("more") || op.equals("max") ) {
			itm.setAmount(itm.getMaxStackSize());
			return Parameter.from(itm.getAmount());
		} else if ( op.equals("damage") || op.equals("dmg") || op.equals("durability") ) {
			if ( args.size() > 0 ) {
				itm.setDurability((short)getArgOrFizzle(ctx, 0, IntegerParameter.class).asInteger().intValue());
			}
			return Parameter.from(itm.getDurability());			
		} else if ( op.equals("repair") || op.equals("fix") ) {
			itm.setDurability((short)0);
			return Parameter.from(0);
		} else if ( op.equals("material") || op.equals("type") ) {
			Material old = itm.getType();
			if ( args.size() > 0 ) {
				Material now = getArgOrFizzle(ctx, 0, MaterialParameter.class).asMaterial();
				itm.setType(now);
			}
			return Parameter.from(itm.getType());
		}
		
		
		
		fizzle("Invalid operation " + op);
		return null; //Never executed.
 	}

	
}
