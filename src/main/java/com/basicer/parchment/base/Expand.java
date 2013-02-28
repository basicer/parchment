package com.basicer.parchment.base;



import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Location;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.BlockParameter;
import com.basicer.parchment.parameters.DoubleParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

public class Expand extends Spell {

	public Parameter affect(LocationParameter target, Context ctx) {
		return expand(target, ctx);
	}
	
	public Parameter affect(BlockParameter target, Context ctx) {
		return expand(target, ctx);
	}
	
	@Override
	protected boolean getShouldCombindDuplicateListOutput() {
		return true;
	}

	private Parameter expand(Parameter target, Context ctx) {
		ListParameter l = ctx.get("args").cast(ListParameter.class);
		Queue<Parameter> args = new LinkedList<Parameter>(l.asArrayList());
		
		if ( args.size() < 1 ) return ctx.getTarget();
		
		ArrayList<Location> locations = new ArrayList<Location>();
		for ( Parameter p : target ) {
			Location loc = p.as(Location.class);
			if ( loc != null ) locations.add(loc);
		}
		
		ctx.sendDebugMessage("Target is: " + target.toString());
		Class<? extends Parameter> type = target.getClass();
		
		if ( args.peek() != null ) {
			ArrayList<Location> n = new ArrayList<Location>();
			
			Location loc = target.as(Location.class);
			int ammount = args.poll().asInteger();
			for ( int x = loc.getBlockX() - ammount; x <= loc.getBlockX() + ammount; ++x ) {
				for ( int z = loc.getBlockZ() - ammount; z <= loc.getBlockZ() + ammount; ++z ) {
					Location o = loc.clone();
					o.setX(x);
					o.setZ(z);
					n.add(o);
					
				}
			}
			
			locations = n;
			
		}
		
		ArrayList<Parameter> out = new ArrayList<Parameter>();
		for ( Location ll : locations ) {
			if ( type == BlockParameter.class ) {
				out.add(Parameter.from(ll.getWorld().getBlockAt(ll)));
			}
		}
		
		Parameter outp;
		if ( out.size() == 1 ) outp = out.get(0);
		else outp = Parameter.createList(out.toArray(new Parameter[0]));
		
				
		return outp;
	}

}
