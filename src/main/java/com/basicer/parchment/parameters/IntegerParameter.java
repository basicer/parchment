package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;

public class IntegerParameter extends Parameter {
	private Long self;
	
	public IntegerParameter(Integer self) {
		this.self = self.longValue();
	}

	public IntegerParameter(Long self) {
		this.self = self;
	}

	@Override
	public Class<Long> getUnderlyingType() { return Long.class; }
	
	
	public Double asDouble(Context ctx) {
		return Double.valueOf(self);
	}
	
	
	public Integer asInteger(Context ctx) {
		return self.intValue();
	}

	public Long asLong(Context ctx) {
		return self;
	}
	
	
	public String asString(Context ctx) {
		return "" + self;
	}
	
	
	public boolean asBoolean(Context ctx) {
		return ( self != 0 );
	}

	public boolean asBooleanStrict(Context ctx) {
		return ( self != 0 );
	}


	public int intValue() {
		return self.intValue();
	}


}
