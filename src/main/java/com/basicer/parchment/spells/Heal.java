package com.basicer.parchment.spells;

import org.bukkit.entity.LivingEntity;

import com.basicer.parchment.Affectable;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Context;
import com.basicer.parchment.parameters.*;
import com.mysql.jdbc.log.Log;

public class Heal extends Spell implements Affectable<PlayerParameter> {

	public void affect(PlayerParameter target, Context ctx) {
		LivingEntity ltarget = target.asLivingEntity();
		if ( ltarget == null ) fizzle();		
		ltarget.setHealth(20); 
 	}

}
