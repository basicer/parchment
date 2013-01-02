package com.basicer.parchment.unsafe;

public interface ParchmentNBTBase extends ProxyInterface {

	public abstract String getName();
	public abstract void setName(String n);
	public abstract ParchmentNBTBase copy();
	public abstract byte getTypeId();
	
		
}
