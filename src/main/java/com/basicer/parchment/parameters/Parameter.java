package com.basicer.parchment.parameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;


public abstract class Parameter implements Iterable<Parameter> {

	public enum SelectionMode { DEFAULT, HIT, LOOKING, STANDING };
	
	public Location asLocation() { return asLocation(SelectionMode.DEFAULT); }
	public Location asLocation(SelectionMode mode) { return null; }
	
	public LivingEntity asLivingEntity() { return null; }
	public Entity asEntity() { return null; }
	public Player asPlayer() { return null; }
	public String asString() { return null; }
	public Double asDouble() { return null; }
	public Integer asInteger() { return null; }
	public World asWorld() { return null; }
	public Server asServer() { return null; }
	public ItemStack asItemStack() { return null; }
	
	public <T extends Parameter> T cast(Class<T> type) {
		if ( type.equals(EntityParameter.class) ) {
			return (T)Parameter.from(this.asEntity());
		} else if ( type.equals(PlayerParameter.class) ) {
			return (T)Parameter.from(this.asPlayer());
		} else if ( type.equals(StringParameter.class) ) {
			return (T)Parameter.from(this.asString());
		} else if ( type.equals(DoubleParameter.class) ) {
			return (T)Parameter.from(this.asDouble());
		} else if ( type.equals(WorldParameter.class) ) {
			return (T)Parameter.from(this.asWorld());
		} else if ( type.equals(ServerParameter.class) ) {
			return (T)Parameter.from(this.asServer());
		} else if ( type.equals(ItemParameter.class) ) {
			return (T)Parameter.from(this.asItemStack());
		}
		
		return null;
	}

	public Double asDoubleOr(Double def) {
		Double val = asDouble();
		if ( val != null ) return val;
		return def;
	}
	
	public Integer asIntegerOr(int def) {
		Integer val = asInteger();
		if ( val != null ) return val;
		return def;
	}
	
	// Factory methods
	public static Parameter from(Player p) 		{ return new PlayerParameter(p); }
	public static Parameter from(Entity e) 		{ return new EntityParameter(e); }
	public static Parameter from(String s) 		{ return new StringParameter(s); }
	public static Parameter from(double d) 		{ return new DoubleParameter(d); }
	public static Parameter from(int i) 		{ return new DoubleParameter(i); }
	public static Parameter from(World w) 		{ return new WorldParameter(w); }
	public static Parameter from(Server s) 		{ return new ServerParameter(s); }
	public static Parameter from(ItemStack s) 	{ return new ItemParameter(s); }
	
	
	public static Parameter createList(Parameter[] list) {
		return createList(list, 0, list.length - 1);
	}
	
	public static Parameter createList(Parameter[] list, int start, int end) {
		List<Parameter> nlist = new ArrayList<Parameter>();
		for ( int i = start; i <= end; ++i ) {
			nlist.add(list[i]);
		}
		return new ListParameter(nlist);
	}

	
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
