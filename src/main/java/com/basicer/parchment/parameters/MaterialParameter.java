package com.basicer.parchment.parameters;

import org.bukkit.Material;

import com.basicer.parchment.Context;

public class MaterialParameter extends Parameter {
	private Material self;
	MaterialParameter(Material self) {
		this.self = self;
	}
	
	@Override
	public String asString(Context ctx) { return self.name(); }
	
	@Override
	public Integer asInteger(Context ctx) { return self.getId(); }

	@Override
	public Material asMaterial(Context ctx) { return self; }
}
