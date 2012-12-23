package com.basicer.parchment.spells;

import org.bukkit.entity.LivingEntity;

import com.basicer.parchment.Affectable;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Context;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.parameters.*;
import com.mysql.jdbc.log.Log;

public class Heal extends Spell implements Affectable<PlayerParameter> {
	
	@Override
	public String[] getArguments() { return new String[] { "ammount" }; }

	@Override
	public DefaultTargetType getDefaultTargetType() { return DefaultTargetType.Self; }
	
	public void affect(PlayerParameter target, Context ctx) {
		LivingEntity ltarget = target.asLivingEntity();
		if ( ltarget == null ) fizzle();
		 
		double health = ltarget.getHealth() / 2.0;
		double ammt = ctx.get("ammount").asDoubleOr(10.0 - health);
		int new_health = (int)((health + ammt) * 2);
		
		if ( new_health > 20 ) new_health = 20;
		if ( new_health < 0 ) new_health = 0;

		ltarget.setHealth(new_health); 
 	}

}
