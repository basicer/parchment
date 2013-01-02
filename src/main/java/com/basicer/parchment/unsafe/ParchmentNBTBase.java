package com.basicer.parchment.unsafe;

public interface ParchmentNBTBase {

	public abstract String getName();
	public abstract void setName(String n);
	public abstract ParchmentNBTBase copy();
	public abstract byte getTypeId();
	
	public abstract Object unproxy();
	
}
