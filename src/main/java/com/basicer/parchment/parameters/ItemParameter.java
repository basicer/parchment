package com.basicer.parchment.parameters;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;

public class ItemParameter extends Parameter {

	private ItemStack self;
	public ItemParameter(ItemStack self) {
		this.self = self;
	}
	
	@Override
	public Class<ItemStack> getUnderlyingType() { return ItemStack.class; }
	
	
	public ItemStack asItemStack(Context ctx) { return self; }
	
	
	public String asString(Context ctx) { 
		ItemMeta m = self.getItemMeta();
		if ( m != null && m.getDisplayName() != null ) return m.getDisplayName();
		return self.getType().name(); 
	}
}
