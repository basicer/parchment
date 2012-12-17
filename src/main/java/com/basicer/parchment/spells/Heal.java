package com.basicer.parchment.spells;

import org.bukkit.entity.LivingEntity;

import com.basicer.parchment.Affectable;
import com.basicer.parchment.Spell;
import com.basicer.parchment.SpellContext;
import com.basicer.parchment.parameters.*;
import com.mysql.jdbc.log.Log;

public class Heal extends Spell implements Affectable<PlayerParameter> {

	public void affect(PlayerParameter target, SpellContext ctx) {
		LivingEntity ltarget = target.asLivingEntity();
		ctx.sendDebugMessage("Casting...");
		if ( target == null ) return;
		
		ltarget.setHealth(20); 
		ctx.sendDebugMessage("Done");
	}

}
