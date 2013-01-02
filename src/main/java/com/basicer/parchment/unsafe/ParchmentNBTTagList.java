package com.basicer.parchment.unsafe;

public interface ParchmentNBTTagList extends ParchmentNBTBase {
	public abstract	ParchmentNBTBase tagAt(int i);
	public abstract int tagCount();
	public abstract ParchmentNBTBase get(int i);
	
}
