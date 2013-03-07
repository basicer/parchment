package com.basicer.parchment.spells;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.gui.Button;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;

public class Spout extends Spell {


	public Parameter affect(PlayerParameter who, Context ctx) {
		
		Player p = who.as(Player.class);
		SpoutPlayer sp = (SpoutPlayer) p;
		Button b = new GenericButton();
		b.setWidth(10);
		b.setHeight(10);
		b.setText("Rob");
		sp.getMainScreen().attachWidget(Bukkit.getPluginManager().getPlugin("Parchment"), b);
		
		
		return who;
	}
}
