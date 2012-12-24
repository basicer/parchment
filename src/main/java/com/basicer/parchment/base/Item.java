package com.basicer.parchment.base;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Affectable;
import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.PlayerParameter;

public class Item extends Spell implements Affectable<ItemParameter> {

	@Override
	public DefaultTargetType getDefaultTargetType() { return DefaultTargetType.Self; }
	
	@Override
	public String[] getArguments() { return new String[] { "name" }; }
	
	public void affect(ItemParameter target, Context ctx) {
		ItemStack itm = target.asItemStack();
		String str = ctx.get("name").asString();
		
		ctx.sendDebugMessage("Old Name>" + itm.getItemMeta().getDisplayName());
		ItemMeta m = itm.getItemMeta();
		m.setDisplayName(str);
		ctx.sendDebugMessage("New Name>" + str);
		itm.setItemMeta(m);
 	}
	
}
