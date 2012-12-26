package com.basicer.parchment.parameters;

import org.bukkit.Material;

import com.basicer.parchment.Context;

public class IntegerParameter extends Parameter {
	private int self;
	
	public IntegerParameter(int self) {
		this.self = self;
	}

	@Override
	public Double asDouble(Context ctx) {
		return Double.valueOf(self);
	}
	
	@Override
	public Integer asInteger(Context ctx) {
		return self;
	}
	
	
	@Override
	public String asString(Context ctx) {
		return "" + self;
	}
	
	@Override
	public boolean asBoolean(Context ctx) {
		return ( self == 0 );
	}
	
	@Override
	public Material asMaterial(Context ctx) {
		return Material.getMaterial((int)self);
	}
}
