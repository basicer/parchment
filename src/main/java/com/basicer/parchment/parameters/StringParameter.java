package com.basicer.parchment.parameters;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;

public class StringParameter extends Parameter {
	private String self;
	
	public StringParameter(String str) {
		self = str;
	}

	@Override
	public Class<String> getUnderlyingType() { return String.class; }
	
	
	public Double asDouble(Context ctx) {
		try { 
			return Double.parseDouble(self);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	
	public Integer asInteger(Context ctx) {
		try {
			return (int)(Double.parseDouble(self));
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	@Override
	public String asString(Context ctx) { return self; }

	
	
}

