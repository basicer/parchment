package com.basicer.parchment.parameters;

public class NumericParameter extends Parameter {
	private double self;
	
	public NumericParameter(double self) {
		this.self = self;
	}

	@Override
	public Double asDouble() {
		return self;
	}
}
