package com.basicer.parchment.spells;

import org.bukkit.entity.LivingEntity;

import com.basicer.parchment.TargetedCommand;
import com.basicer.parchment.Context;
import com.basicer.parchment.parameters.*;

public class Heal extends TargetedCommand {
	
	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
	}

	@Override
	public String[] getArguments() { return new String[] {"amount?"}; }
	
	public Parameter affect(PlayerParameter target, Context ctx) {
		LivingEntity ltarget = target.as(LivingEntity.class);
		if ( ltarget == null ) fizzle();
		 
		double health = ltarget.getHealth();
		double maxhealth = ltarget.getMaxHealth();
		double ammt = ctx.getWithTypeOr("amount", Parameter.from(maxhealth  - health)).asDouble();
		double new_health = (health + ammt);
		
		if ( new_health > ltarget.getMaximumAir() ) new_health = ltarget.getMaxHealth();
		if ( new_health < 0 ) new_health = 0;

		ltarget.setHealth(new_health);
		return Parameter.from(new_health);
 	}

	public Parameter affect(EntityParameter target, Context ctx) {
		LivingEntity ltarget = target.as(LivingEntity.class);
		if ( ltarget == null ) fizzle("Entity null or not alive");
		 
		double health = ltarget.getHealth() / 2.0;
		double ammt = ctx.getWithTypeOr("amount", Parameter.from(ltarget.getMaxHealth() - health)).asDouble();
		double new_health = (health + ammt) * 2;
		
		if ( new_health > ltarget.getMaxHealth() ) new_health = ltarget.getMaxHealth();
		if ( new_health < 0 ) new_health = 0;

		ltarget.setHealth(new_health);
		return Parameter.from(new_health);
 	}

	
}
