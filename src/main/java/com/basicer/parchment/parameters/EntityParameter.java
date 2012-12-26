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
	public Location asLocation(Context ctx, SelectionMode mode) {
		return self.getLocation();
	}

	@Override
	public Entity asEntity(Context ctx) {
		return self;
	}
	
}
