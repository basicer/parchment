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
	public Double asDouble(Context ctx) {
		return Double.parseDouble(self);
	}
	
	@Override
	public Integer asInteger(Context ctx) {
		return (int)((double)asDouble(ctx));
	}
	
	@Override
	public String asString(Context ctx) { return self; }
	
	@Override
	public Player asPlayer(Context ctx) {
		if ( ctx == null ) return null;
		Server s = ctx.getServer();
		if ( s == null ) return null;
		System.out.println("Server s looks for " + self);
		return s.getPlayer(self);
		
	}
	
	@Override
	public World asWorld(Context ctx) {
		if ( ctx == null ) return null;
		Server s = ctx.getServer();
		if ( s == null ) return null;
		
		return Bukkit.getWorld(self);		
	}
	
	@Override
	public Material asMaterial(Context ctx) {
		Material m = Material.matchMaterial(self);
		if ( m == null ) return null;
		return m;
	}
	
}

