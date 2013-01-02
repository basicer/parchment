package com.basicer.parchment.unsafe;

public interface ParchmentNBTTagList extends ParchmentNBTBase {
	public abstract	ParchmentNBTBase tagAt(int i);
	public abstract int size();
	public abstract ParchmentNBTBase get(int i);
	public abstract void add(@Unwrap ParchmentNBTBase node);
	
}
