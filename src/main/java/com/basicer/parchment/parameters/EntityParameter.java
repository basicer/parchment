package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.basicer.parchment.Context;



public class EntityParameter extends Parameter {

	private Entity self;
	EntityParameter(Entity self) {
		this.self = self;
	}
	
	@Override
	public Class<? extends Entity> getUnderlyingType() { return Entity.class; }
	
	

	public Location asLocation(Context ctx) {
		return self.getLocation();
	}
	
	public Entity asEntity(Context ctx) {
		return self;
	}
	
	
	public LivingEntity asLivingEntity(Context ctx) {
		if ( !(self instanceof LivingEntity)) return null;
		return (LivingEntity) self;
		
	}

	public String asString(Context ctx) {
		Location l = self.getLocation();
		return "[Entity T:" + self.getType().name() + " @ " + self.getWorld().getName() + "/" 
				+ l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() +
				"]";
	}
}
