package com.basicer.parchment.parameters;

import org.bukkit.Location;

import com.basicer.parchment.Context;

public class LocationParameter extends Parameter {
	private Location self;
	LocationParameter(Location self) {
		this.self = self;
	}
	
	@Override
	public Class<Location> getUnderlyingType() { return Location.class; }
	
	/*
	@Override
	public Location asLocation(Context ctx, SelectionMode mode) {
		return self;
	}
	*/
	
	public Location asLocation(Context ctx) {
		return self;
	}
	
	@Override
	public String asString(Context ctx) {
		return "[Location T:" + self.getWorld().getName() + "/" 
				+ self.getBlockX() + "," + self.getBlockY() + "," + self.getBlockZ() +
				"]";
	}

	@Override
	public Parameter index(String s) {
		if ( s.equalsIgnoreCase("X") ) return Parameter.from(self.getX());
		if ( s.equalsIgnoreCase("Y") ) return Parameter.from(self.getY());
		if ( s.equalsIgnoreCase("Z") ) return Parameter.from(self.getZ());
		
		if ( s.equalsIgnoreCase("BX") ) return Parameter.from(self.getBlockX());
		if ( s.equalsIgnoreCase("BY") ) return Parameter.from(self.getBlockY());
		if ( s.equalsIgnoreCase("BZ") ) return Parameter.from(self.getBlockZ());
		
		if ( s.equalsIgnoreCase("pitch") ) return Parameter.from(self.getPitch());
		if ( s.equalsIgnoreCase("yaw") ) return Parameter.from(self.getYaw());
		
		
		return super.index(s);
	}
	
	@Override
	public void writeIndex(String s, Parameter p) {
		double val = p.asDouble();
		if ( s.equalsIgnoreCase("X") ) self.setX(val);
		if ( s.equalsIgnoreCase("Y") ) self.setY(val);
		if ( s.equalsIgnoreCase("Z") ) self.setZ(val);
		
		if ( s.equalsIgnoreCase("pitch") ) self.setPitch((float) val);
		if ( s.equalsIgnoreCase("yaw") ) self.setYaw((float) val);
		
		
		super.writeIndex(s, p);
	}
	
	
}
