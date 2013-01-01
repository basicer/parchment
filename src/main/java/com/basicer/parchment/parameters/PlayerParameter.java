package com.basicer.parchment.parameters;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.basicer.parchment.Context;


public class PlayerParameter extends EntityParameter {

	private Player self;
	PlayerParameter(Player self) {
		super(self);
		this.self = self;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Class getUnderlyingType() { return Player.class; }
	
	@Override
	public Player asPlayer(Context ctx) { return self; }
	
	@Override
	public LivingEntity asLivingEntity(Context ctx) { return self; }

	@Override
	public String asString(Context ctx) { return self.getName(); }

	@Override
	public ItemStack asItemStack(Context ctx) { return self.getItemInHand(); }
	
	 
}
