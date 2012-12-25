package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;

public class DoubleParameter extends Parameter {
	private double self;
	
	public DoubleParameter(double self) {
		this.self = self;
	}

	@Override
	public Double asDouble(Context ctx) {
		return self;
	}
	
	@Override
	public Integer asInteger(Context ctx) {
		return Integer.valueOf((int) self);
	}
	
	
	@Override
	public String asString(Context ctx) {
		return "" + self;
	}
}
