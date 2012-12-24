package com.basicer.parchment.parameters;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ItemParameter extends Parameter {

	private ItemStack self;
	ItemParameter(ItemStack self) {
		this.self = self;
	}
	
	@Override
	public ItemStack asItemStack() { return self; }
}
