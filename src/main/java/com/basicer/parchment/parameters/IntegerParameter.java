package com.basicer.parchment.parameters;

import org.bukkit.Material;

import com.basicer.parchment.Context;

public class IntegerParameter extends Parameter {
	private Integer self;
	
	public IntegerParameter(Integer self) {
		this.self = self;
	}

	@Override
	public Class<Integer> getUnderlyingType() { return Integer.class; }
	
	
	public Double asDouble(Context ctx) {
		return Double.valueOf(self);
	}
	
	
	public Integer asInteger(Context ctx) {
		return self;
	}
	
	
	
	public String asString(Context ctx) {
		return "" + self;
	}
	
	
	public boolean asBoolean(Context ctx) {
		return ( self != 0 );
	}
	
	
	public Material asMaterial(Context ctx) {
		return Material.getMaterial((int)self);
	}
	

}
