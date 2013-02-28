package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;

public class DoubleParameter extends Parameter {
	private Double self;
	
	public DoubleParameter(Double self) {
		this.self = self;
	}

	@Override
	public Class<Double> getUnderlyingType() { return Double.class; }
	
	
	public Double asDouble(Context ctx) {
		return self;
	}
	
	
	public Integer asInteger(Context ctx) {
		return Integer.valueOf(self.intValue());
	}
	
	
	
	public String asString(Context ctx) {
		return "" + self;
	}

	public Parameter downCastIfPossible() {
		if ( self.intValue() == self.doubleValue() ) {
			return Parameter.from(self.intValue());
		}
		return this;
	}
	
}
