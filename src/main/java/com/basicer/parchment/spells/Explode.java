package com.basicer.parchment.spells;

import org.bukkit.Location;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.Spell;
import com.basicer.parchment.parameters.Parameter;

public class Explode extends Spell {

	
	


	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
	}

	@Override
	public String[] getArguments() { return new String[] { "-nobreak", "-nofire", "power?" }; }

	public Parameter affect(Parameter location, Context ctx) {
		Location l = location.as(Location.class);
		
		double power = 4;
		boolean breakblock = true;
		boolean fire = true;
		Parameter powerp = ctx.get("power");
		
		Debug.info(ctx.getDebuggingString() );
		
		if ( powerp != null ) power = powerp.asDouble();
		if ( ctx.has("nobreak") && ctx.get("nobreak").asBoolean() ) breakblock = false;
		if ( ctx.has("nofire") && ctx.get("nofire").asBoolean() ) fire = false;
		
		l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), (float)power, fire, breakblock);
		
		
		return null;
	}
}
