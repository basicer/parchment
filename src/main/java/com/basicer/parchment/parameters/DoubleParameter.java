package com.basicer.parchment.parameters;

public class DoubleParameter extends Parameter {
	private double self;
	
	public DoubleParameter(double self) {
		this.self = self;
	}

	@Override
	public Double asDouble() {
		return self;
	}
	
	@Override
	public String asString() {
		return "" + self;
	}
}
