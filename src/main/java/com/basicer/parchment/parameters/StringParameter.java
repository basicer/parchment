package com.basicer.parchment.parameters;

public class StringParameter extends Parameter {
	private String self;
	
	public StringParameter(String str) {
		self = str;
	}

	@Override
	public Double asDouble() {
		return Double.parseDouble(self);
	}
	
	@Override
	public String asString() { return self; }
	
}

