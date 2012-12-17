package com.basicer.parchment.parameters;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;



public class EntityParameter extends Parameter {

	private Entity self;
	EntityParameter(Entity self) {
		this.self = self;
	}
	
	@Override
	public Location asLocation() {
		return self.getLocation();
	}

	@Override
	public Entity asEntity() {
		return self;
	}
	
}
