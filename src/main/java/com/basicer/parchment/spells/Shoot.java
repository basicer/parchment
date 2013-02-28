package com.basicer.parchment.spells;

import org.bukkit.entity.*;
import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;


public class Shoot extends Spell {
	
	@Override
	public Parameter cast(Context ctx) {
		LivingEntity ent = ctx.getCaster().as(LivingEntity.class);
		if ( ent == null ) fizzle();
		ent.launchProjectile(Arrow.class);
		return null;
	}

}
