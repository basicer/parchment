package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.basicer.parchment.Context;



public class LivingEntityParameter extends EntityParameter {

	private LivingEntity self;
	LivingEntityParameter(LivingEntity self) {
		super(self);
		this.self = self;
	}
	
	@Override
	public Class<? extends Entity> getUnderlyingType() { return Entity.class; }
	

	@Override
	public Entity asEntity(Context ctx) {
		return self;
	}
	
	@Override
	public LivingEntity asLivingEntity(Context ctx) {
		return self;
		
	}

	@Override
	public String asString(Context ctx) {
		Location l = self.getLocation();
		return "[LivingEntity T:" + self.getType().name() + " @ " + self.getWorld().getName() + "/" 
				+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() +
				"]";
	}
}
