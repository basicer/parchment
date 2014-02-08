package com.basicer.parchment.base;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.*;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by basicer on 2/8/14.
 */
public class Find extends TCLCommand {
	@Override
	public String[] getArguments() { return new String[] { "-type=", "-at=", "-dist=", "-limit=", "-one" }; }

	public String getDescription() { return "Find things in loaded chunks"; }

	public enum SearchTypes { ENTITY, PLAYER, MOB, BLOCK };

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		if ( !ctx.has("type") ) return EvaluationResult.makeError("Type is a required flag.");

		final EntityType et = ctx.get("type").asEnum(EntityType.class);
		final Material bt = ctx.get("type").asEnum(Material.class);

		SearchTypes t;
		if ( et != null ) {
			t = SearchTypes.ENTITY;
		} else if ( bt != null ) {
			t = SearchTypes.BLOCK;
		} else {
			t = ctx.get("type").asEnum(SearchTypes.class);
		}

		if ( t == null ) return EvaluationResult.makeError("Don't know how to search for " + ctx.get("type").asString());

		final Double dist = fetchArguementOrFizzle("dist", Double.class, ctx);
		Location xwhere = fetchArguementOrFizzle("at", Location.class, ctx);
		Integer limit = fetchArguementOrFizzle("limit", Integer.class, ctx);

		if ( ctx.has("one") ) {
			if ( limit != null ) return EvaluationResult.makeError("Cant specify both -one and -limit");
			limit = 1;
		}

		if ( dist != null && xwhere == null ) xwhere = ctx.getCaster().as(Location.class);

		List<org.bukkit.World> worlds;
		if ( xwhere != null ) {
			worlds = new ArrayList<org.bukkit.World>();
			worlds.add(xwhere.getWorld());
		} else {
			worlds = Bukkit.getServer().getWorlds();
		}

		final SearchTypes type = t;
		final Location where = xwhere;
		Predicate<org.bukkit.entity.Entity> entityFilter = new Predicate<org.bukkit.entity.Entity>() {
			@Override
			public boolean evaluate(org.bukkit.entity.Entity input) {
				if ( input == null ) return false;
				if ( et != null && !input.getType().equals(et) ) return false;
				if ( type == SearchTypes.MOB && !(input instanceof Monster) ) return false;
				if ( where != null ) {
					if ( !where.getWorld().equals(input.getWorld())) return false;
					if ( input.getLocation().distance(where) > dist ) return false;
				}
				return true;
			}
		};

		Predicate<org.bukkit.block.Block> blockFilter = new Predicate<org.bukkit.block.Block>() {
			@Override
			public boolean evaluate(org.bukkit.block.Block input) {
				if ( input == null ) return false;
				if ( bt != null && !input.getType().equals(bt) ) return false;
				if ( where != null ) {
					if ( !where.getWorld().equals(input.getWorld())) return false;
					if ( input.getLocation().distance(where) > dist ) return false;
				}
				return true;
			}
		};


		final ArrayList<Parameter> result = new ArrayList<Parameter>();


		switch ( t ) {
			case ENTITY:
			case MOB:
				for ( org.bukkit.World w : worlds ) {
					for ( org.bukkit.entity.Entity ee : w.getEntities() ) {
						if ( !entityFilter.evaluate(ee) ) continue;
						result.add(EntityParameter.from(ee));
						if ( limit != null && limit <= result.size() ) break;
					}
				}
				break;
			case PLAYER:
				for ( Player p : Bukkit.getServer().getOnlinePlayers() ) {
					if ( !entityFilter.evaluate(p) ) continue;
					result.add(PlayerParameter.from(p));
					if ( limit != null && limit <= result.size() ) break;
				}
			case BLOCK:
				Location nwhere = where.clone();
				nwhere.setY(60);
				for ( org.bukkit.World w : worlds ) {
					for ( Chunk c : w.getLoadedChunks() ) {
						if ( !c.isLoaded() ) continue;
						if ( where != null && dist != null ) {
							if ( c.getBlock(8,60,8).getLocation().distance(where) > (dist+23) ) continue;
						}

						for ( int x = 0; x < 16; ++x ) for ( int y = 0; y < 128; ++y ) for ( int z = 0; z < 16; ++z ) {
							Block b = c.getBlock(x,y,z);
							if ( !blockFilter.evaluate(b) ) continue;
							result.add(BlockParameter.from(b));
							if ( limit != null && limit <= result.size() ) break;
						}
					}

				}
		}

		if ( ctx.has("one") ) return new EvaluationResult(result.get(0));
		return new EvaluationResult(ListParameter.from(result));
	}

	private <T> T fetchArguementOrFizzle(String id, Class<T> clazz, Context ctx) {
		if ( !ctx.has(id) ) return null;
		Parameter v = ctx.get(id);
		T val = v.as(clazz, ctx);
		if ( val == null ) throw new FizzleException("Expcted type " + clazz.getSimpleName() + " for " + id + " if specified.");
		return val;
	}

	protected interface Predicate<T> {
		public boolean evaluate(T input);
	}

}


