package com.basicer.parchment.parameters;

import java.util.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;

public class ListParameter extends Parameter {

	private List<Parameter> self;
	ListParameter(List<Parameter> self) {
		this.self = self;
	}
	
	@Override
	public LivingEntity asLivingEntity(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.asLivingEntity();
	}

	@Override
	public Entity asEntity(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.asEntity();
	}

	@Override
	public Player asPlayer(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.asPlayer();
	}
	
	@Override
	public String asString(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.asString();
	}

	@Override
	public Iterator<Parameter> iterator() {
		return self.iterator();
	}
	
	public ArrayList<Parameter> asArrayList() {
		return new ArrayList<Parameter>(self);
	}
	


}
