package com.basicer.parchment.parameters;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class PlayerParameter extends EntityParameter {

	private Player self;
	PlayerParameter(Player self) {
		super(self);
		this.self = self;
	}
	
	
	@Override
	public Player asPlayer() { return self; }
	
	@Override
	public LivingEntity asLivingEntity() { return self; }

	@Override
	public String asString() { return self.getName(); }

	@Override
	public ItemStack asItemStack() { return self.getItemInHand(); }
	
}
