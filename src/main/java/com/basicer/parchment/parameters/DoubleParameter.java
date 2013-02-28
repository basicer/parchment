package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;

public class DoubleParameter extends Parameter {
	private double self;
	
	public DoubleParameter(double self) {
		this.self = self;
	}

	@Override
	public Class<Double> getUnderlyingType() { return Double.class; }
	
	
	public Double asDouble(Context ctx) {
		return self;
	}
	
	
	public Integer asInteger(Context ctx) {
		return Integer.valueOf((int) self);
	}
	
	
	
	public String asString(Context ctx) {
		return "" + self;
	}

	public Parameter downCastIfPossible() {
		if ( (int)self == self ) {
			return Parameter.from((int) self);
		}
		return this;
	}
	
}
