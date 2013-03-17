package com.basicer.parchment.parameters;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;

public class StringParameter extends Parameter {
	private String self;
	
	public StringParameter(String str) {
		self = str;
	}

	@Override
	public Class<String> getUnderlyingType() { return String.class; }
	
	
	public Double asDouble(Context ctx) {
		try { 
			return Double.parseDouble(self);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	
	public Integer asInteger(Context ctx) {
		if ( self.startsWith("0o") ) {
			return Integer.parseInt(self.substring(2), 8);
		}
		try {
			return (int)(Double.parseDouble(self));
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	@Override
	public String asString(Context ctx) { return self; }

	@Override
	public boolean asBoolean(Context ctx) {
		if ( self.equalsIgnoreCase("true") ) return true;
		if ( self.equalsIgnoreCase("on") ) return true;
		if ( self.equalsIgnoreCase("off") ) return false;
		if ( self.equalsIgnoreCase("false") ) return false;
		if ( self.length() == 0 ) return false;
		Integer i = asInteger(ctx);
		if ( i == null ) return false;
		return ( i != 0 );
		
	}
	
}

