package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.OperationalSpell;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by basicer on 1/19/14.
 */
public class Firework extends OperationalSpell<EntityParameter> {

	public static Class<? extends OperationalSpell<?>> getBaseClass() { return Entity.class; }

	public Parameter affect(EntityParameter target, Context ctx) {
			return this.doaffect(target, ctx);
	}

	public Parameter affectNull( Context ctx) {
		return this.doaffect(null, ctx, org.bukkit.entity.Firework.class);
	}



	public static org.bukkit.entity.Entity create(Context ctx, LocationParameter where) {
		Location l = where.asLocation(ctx);

		try {
			return (org.bukkit.entity.Firework)l.getWorld().spawnEntity(l, EntityType.FIREWORK);
		} catch ( IllegalArgumentException ex ) {
			fizzle(ex.getMessage());
			return null;
		}
	}

	private static Random random = new Random();

	private static Pattern hexcolor = Pattern.compile("#?([a-zA-Z0-9][a-zA-Z0-9])([a-zA-Z0-9][a-zA-Z0-9])([a-zA-Z0-9][a-zA-Z0-9])");
	private static Color parseColor(String s) {

		switch ( s.toUpperCase() ) {
			case "RANDOM":

				return Color.fromBGR(random.nextInt(255),random.nextInt(255), random.nextInt(255) );
			case "RED": return Color.RED;
			case "BLUE": return Color.BLUE;
			case "GREEN": return Color.GREEN;
			case "WHITE": return Color.WHITE;
			case "PURPLE": return Color.PURPLE;
			case "YELLOW": return Color.YELLOW;
			case "AQUA":case "CYAN": return Color.AQUA;
			case "PINK":case "FUCHSIA": return Color.FUCHSIA;
			case "BLACK": return Color.BLACK;
			case "LIME": return Color.LIME;
			case "ORANGE": return Color.ORANGE;
			case "TEAL": return Color.TEAL;
			case "SILVER": return Color.SILVER;
			case "MAROON": return Color.MAROON;
			case "GRAY": return Color.GRAY;
			case "OLIVE": return Color.OLIVE;
		}

		Matcher hcm = hexcolor.matcher(s);
		if ( hcm.matches() ) {
			return Color.fromRGB(Integer.parseInt(hcm.group(1), 16), Integer.parseInt(hcm.group(2), 16), Integer.parseInt(hcm.group(3), 16));
		}

		return null;
	}

	public static FireworkEffect.Builder builderFromList(ListParameter eff, Context ctx) {
		FireworkEffect.Builder builder = FireworkEffect.builder();
		FireworkEffect.Type[] types = FireworkEffect.Type.values();
		builder.with(types[random.nextInt(types.length)]);

		for ( int i = 0; i < eff.length(); ++i ) {
			switch ( eff.index(i).asString(ctx).toUpperCase() ) {
				case "BALL": builder.with(FireworkEffect.Type.BALL); break;
				case "STAR": builder.with(FireworkEffect.Type.STAR);  break;
				case "BALL_LARGE": builder.with(FireworkEffect.Type.BALL_LARGE);  break;
				case "BURST": builder.with(FireworkEffect.Type.BURST);  break;
				case "CREEPER": builder.with(FireworkEffect.Type.CREEPER);  break;
				case "COLOR":
					if ( ++i >= eff.length() ) fizzle("Ran out of args wile parsing effect");
					builder.withColor(parseColor(eff.index(i).asString(ctx)));
					break;
				case "FADE":
					if ( ++i >= eff.length() ) fizzle("Ran out of args wile parsing effect");
					builder.withFade(parseColor(eff.index(i).asString(ctx)));
					break;
				case "TRAIL":
					builder.withTrail();
					break;
				case "FLICKER":
					builder.withFlicker();
					break;
				default:
					Color test = parseColor(eff.index(i).asString(ctx));
					if ( test != null ) { builder.withColor(test); break; }
			}
		}


		return builder;
	}

	@Operation(aliases = {"ae", "add"})
	public static Parameter addeffectOperation(org.bukkit.entity.Firework fent, Context ctx, ListParameter eff) {
		FireworkMeta meta = fent.getFireworkMeta();

		if ( eff == null ) fizzle("addEffect takes an effect descriptor");


		FireworkEffect effect = builderFromList(eff, ctx).build();

		meta.addEffect(effect);
		fent.setFireworkMeta(meta);

		return EntityParameter.from(fent);
	}

	@Operation(aliases = {"explode"})
	public static Parameter detonateOperation(org.bukkit.entity.Firework fent, Context ctx) {
		fent.detonate();
		return EntityParameter.from(fent);
	}

	public static Parameter powerOperation(org.bukkit.entity.Firework fent, Context ctx, IntegerParameter set) {
		FireworkMeta meta = fent.getFireworkMeta();
		if ( set != null ) {
			meta.setPower(set.asInteger());
			fent.setFireworkMeta(meta);
		}
		return IntegerParameter.from(meta.getPower());
	}


}
