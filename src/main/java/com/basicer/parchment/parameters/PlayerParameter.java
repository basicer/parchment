package com.basicer.parchment.parameters;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;


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


}
