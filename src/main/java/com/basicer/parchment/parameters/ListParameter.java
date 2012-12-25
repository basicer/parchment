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
		if ( self.size() == 1 ) {
			Parameter p = self.get(0);
			if ( p == null ) return null;
			return p.asString();
		}
		StringBuilder b = new StringBuilder();
		for ( Parameter p : self) {
			if ( b.length() > 0 ) b.append(" ");
			String x = p.asString();
			if ( x == null || x.length() < 1 ) b.append("{}");
			else if ( x.contains(" ") || x.contains("\\") ) {
				b.append('{');
				b.append(x);
				b.append('}');
			} else {
				b.append(x);
			}
		}
		return b.toString();
	}

	@Override
	public Iterator<Parameter> iterator() {
		return self.iterator();
	}
	
	public ArrayList<Parameter> asArrayList() {
		return new ArrayList<Parameter>(self);
	}
	


}
