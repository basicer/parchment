package com.basicer.parchment.parameters;

import java.util.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class ListParameter extends Parameter {

	private List<Parameter> self;
	
	
	@Override
	public LivingEntity asLivingEntity() {
		if ( self.size() != 1 ) return null;
		return self.get(0).asLivingEntity();
	}

	@Override
	public Entity asEntity() {
		if ( self.size() != 1 ) return null;
		return self.get(0).asEntity();
	}

	@Override
	public Player asPlayer() {
		if ( self.size() != 1 ) return null;
		return self.get(0).asPlayer();
	}

	@Override
	public Iterator<Parameter> iterator() {
		return self.iterator();
	}
	


}
