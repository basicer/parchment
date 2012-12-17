package com.basicer.parchment;


public interface Affectable<T> {
	public void affect(T target, SpellContext ctx);
}

