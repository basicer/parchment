package com.basicer.parchment.parameters;

import org.bukkit.Material;

import com.basicer.parchment.Context;

public class MaterialParameter extends Parameter {
	private Material self;

	MaterialParameter(Material self) {
		this.self = self;
	}
	
	@Override
	public Class<Material> getUnderlyingType() { return Material.class; }
	
	@Override
	public String asString(Context ctx) { return self.name(); }
	
	
	public Integer asInteger(Context ctx) { return self.getId(); }

	
	public Material asMaterial(Context ctx) { return self; }
}
