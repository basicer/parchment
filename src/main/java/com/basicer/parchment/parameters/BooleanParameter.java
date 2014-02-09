package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;

public class BooleanParameter extends Parameter {
	private Boolean self;
	
	public BooleanParameter(Boolean self) {
		this.self = self;
	}

	@Override
	public Class<Boolean> getUnderlyingType() { return Boolean.class; }
	
	
	public Double asDouble(Context ctx) {
		return self ? 1.0 : 0.0;
	}
	
	
	public Integer asInteger(Context ctx) {
		return self ? 1 : 0;
	}
	
	
	
	public String asString(Context ctx) {
		return self ? "true" : "false";
	}
	
	
	public boolean asBoolean(Context ctx) {
		return self;
	}

	public boolean asBooleanStrict(Context ctx) {
		return self;
	}




}
