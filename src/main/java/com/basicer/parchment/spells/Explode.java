package com.basicer.parchment.spells;

import org.bukkit.Location;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.DefaultTargetType;
import com.basicer.parchment.Spell.FirstParamaterTargetType;
import com.basicer.parchment.parameters.Parameter;

public class Explode extends Spell {

	
	


	@Override
	public FirstParamaterTargetType getFirstParamaterTargetType(Context ctx) {
		return FirstParamaterTargetType.FuzzyMatch;
	}

	@Override
	public String[] getArguments() { return new String[] { "power?" }; }

	public Parameter affect(Parameter location, Context ctx) {
		Location l = location.asLocation();
		
		double power = 10;
		Parameter powerp = ctx.get("power");
		if ( powerp != null ) power = powerp.asDouble();
		
		l.getWorld().createExplosion(l, (float)power);
		
		
		return null;
	}
}
