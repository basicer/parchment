package com.basicer.parchment.spells;

import org.bukkit.entity.*;
import com.basicer.parchment.*;


public class Shoot extends Spell {
	
	@Override
	public void cast(Context ctx) {
		LivingEntity ent = ctx.getCaster().asLivingEntity();
		if ( ent == null ) fizzle();
		ent.launchProjectile(Arrow.class);
	}

}
