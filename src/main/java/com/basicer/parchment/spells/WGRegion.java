package com.basicer.parchment.spells;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.parameters.*;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.regions.Region;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 7/9/13
 * Time: 1:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class WGRegion extends OperationalTCLCommand {
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}

	@Override
	public boolean supportedByServer() {
		return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
	}


	private WorldGuardPlugin getWorldGuard() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");

		// WorldGuard may not be loaded
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			throw new FizzleException("Cant find WorldGuard");
		}

		return (WorldGuardPlugin) plugin;
	}


	@Operation(desc = "Returns a list or regions at a given location.")
	public Parameter atOperation(Parameter dummy, Context ctx, Parameter where) {
		LocationParameter loc = where.cast(LocationParameter.class);
		if ( loc != null ) {
			Location xloc = loc.asLocation(ctx);
			ApplicableRegionSet set = getWorldGuard().getRegionManager(xloc.getWorld()).getApplicableRegions(xloc);
			ArrayList<Parameter> out = new ArrayList<Parameter>();
			for (ProtectedRegion pr : set ) {
				ArrayList<Parameter> itm = new ArrayList<Parameter>();
				itm.add(Parameter.from(pr.getId()));
				itm.add(WorldParameter.from(xloc.getWorld()));
				out.add(ListParameter.from(itm));
			}
			return ListParameter.from(out);
		}

		return Parameter.EmptyString;
	}

	private class RegionParseResult {
		public ProtectedRegion region;
		public World world;
	}
	private RegionParseResult regionFromParamater(ListParameter idx, Context ctx) {
		WorldParameter world = idx.length() > 1 ? idx.index(1).cast(WorldParameter.class) : null;
		Parameter where = idx.index(0);
		World bworld = world == null ? ctx.getWorld() : world.asWorld(ctx);
		ProtectedRegion region = getWorldGuard().getRegionManager(bworld).getRegion(where.asString());
		if ( region == null ) return null;
		RegionParseResult out = new RegionParseResult();
		out.region = region;
		out.world = bworld;
		return out;
	}

	@Operation(desc = "Returns a list of players inside a named region.")
	public Parameter playersOperation(Parameter dummy, Context ctx, ListParameter idx) {
		RegionParseResult rgn = regionFromParamater(idx, ctx);
		if ( rgn == null ) throw new FizzleException("Region not found.");
		ArrayList<Parameter> out = new ArrayList<Parameter>();

		for ( Player p : rgn.world.getPlayers() ) {
			Location loc = p.getLocation();
			if ( rgn.region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) ) out.add(Parameter.from(p));
		}

		return ListParameter.from(out);
	}

	@Operation(desc = "Returns a list of points defining a region.")
	public Parameter pointsOperation(Parameter dummy, Context ctx, ListParameter idx) {
		RegionParseResult rgn = regionFromParamater(idx, ctx);
		if ( rgn == null ) throw new FizzleException("Region not found.");
		ArrayList<Parameter> out = new ArrayList<Parameter>();

		for ( BlockVector2D p : rgn.region.getPoints() ) {
			Location loc = new Location( rgn.world, p.getBlockX(), 0, p.getBlockZ());
			out.add(Parameter.from(loc));
		}

		return ListParameter.from(out);
	}


}
