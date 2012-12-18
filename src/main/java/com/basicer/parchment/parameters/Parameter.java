package com.basicer.parchment.parameters;

import java.util.Iterator;

import org.bukkit.*;
import org.bukkit.entity.*;


public abstract class Parameter implements Iterable<Parameter> {

	public enum SelectionMode { DEFAULT, HIT, LOOKING, STANDING };
	
	public Location asLocation() { return asLocation(SelectionMode.DEFAULT); }
	public Location asLocation(SelectionMode mode) { return null; }
	
	public LivingEntity asLivingEntity() { return null; }
	public Entity asEntity() { return null; }
	public Player asPlayer() { return null; }
	
	public Double asDouble() { return null; }
		
	// Factory methods
	public static Parameter from(Player p) { return new PlayerParameter(p); }
	public static Parameter from(Entity p) { return new EntityParameter(p); }
	
	public Iterator<Parameter> iterator() {
		return new SingleIterator<Parameter>(this);
	}
	
	private class SingleIterator<T> implements Iterator<T> {

		private T one;
		SingleIterator(T one) {
			this.one = one;
		}
		
		public boolean hasNext() {
			return this.one != null;
		}

		public T next() {
			if ( this.one != null ) {
				T out = this.one;
				this.one = null;
				return out;
			}
			return null;
		}

		public void remove() { }
		
	}
	
}
