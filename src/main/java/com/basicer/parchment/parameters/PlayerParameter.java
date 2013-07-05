package com.basicer.parchment.parameters;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.basicer.parchment.Context;


public class PlayerParameter extends LivingEntityParameter {

	private Player self;
	PlayerParameter(Player self) {
		super(self);
		this.self = self;
	}
	
	@Override
	public Class<? extends Entity> getUnderlyingType() { return Player.class; }
	
	
	public Player asPlayer(Context ctx) { return self; }
	
	@Override
	public LivingEntity asLivingEntity(Context ctx) { return self; }

	@Override
	public String asString(Context ctx) { return self.getName(); }

	
	public ItemStack asItemStack(Context ctx) { return self.getItemInHand(); }
	
	public Server asServer(Context ctx) { return self.getServer(); }
	public World asWorld(Context ctx) { return self.getWorld(); }
	
	public static PlayerParameter castFrom(StringParameter from, Context ctx) {

		Server s = Bukkit.getServer();
		if ( s == null ) return null;

		Player p = s.getPlayer(from.asString());
		if ( p == null ) return null;
		return new PlayerParameter(p);
	}
}
