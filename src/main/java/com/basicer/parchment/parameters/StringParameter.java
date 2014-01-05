package com.basicer.parchment.parameters;

import org.bukkit.*;
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
	
	
	public Long asLong(Context ctx) {
		
		if ( self.startsWith("0o") ) {
			return Long.valueOf(self.substring(2), 8);
		}
		if ( self.startsWith("0x") ) {
			return Long.valueOf(self.substring(2), 16);
		}
		try {
			return (long)(Double.parseDouble(self));
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public Integer asInteger(Context ctx) {
		Long l = this.asLong(ctx);
		if ( l == null ) return null;
		return l.intValue();
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

