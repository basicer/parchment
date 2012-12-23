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
	public String asString() { return null; }
	public Double asDouble() { return null; }

	public Double asDoubleOr(Double def) {
		Double val = asDouble();
		if ( val != null ) return val;
		return def;
	}
	
	// Factory methods
	public static Parameter from(Player p) { return new PlayerParameter(p); }
	public static Parameter from(Entity e) { return new EntityParameter(e); }
	public static Parameter from(String s) { return new StringParameter(s); }
	public static Parameter from(double d) { return new NumericParameter(d); }
	
	
	public Iterator<Parameter> iterator() {
		return new SingleIterator<Parameter>(this);
	}
	
	@Override
	public String toString() { 
		String as = this.asString();
		String type = this.getClass().getSimpleName();
		if ( as != null ) return "[" + type + ": " + as + "]";
		return "[" + type + "]";
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
