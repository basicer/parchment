package com.basicer.parchment.parameters;

public class StringParameter extends Parameter {
	private String self;
	
	public StringParameter(String str) {
		self = str;
	}

	@Override
	public String asString() { return self; }
	
	@Override
	public String toString() { return self; }
}

