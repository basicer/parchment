package com.basicer.parchment.parameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;


public abstract class Parameter implements Iterable<Parameter> {

	public enum SelectionMode { DEFAULT, HIT, LOOKING, STANDING };
	

	public final Location asLocation() { return asLocation(null, SelectionMode.DEFAULT); }
	public final Location asLocation(SelectionMode mode) { return asLocation(null, mode); }
	
	public final LivingEntity asLivingEntity() { return asLivingEntity(null); }
	public final Entity asEntity() 			{ return asEntity(null); }
	public final Player asPlayer() 			{ return asPlayer(null); }
	public final String asString() 			{ return asString(null); }
	public final Double asDouble() 			{ return asDouble(null); }
	public final Integer asInteger() 		{ return asInteger(null); }
	public final boolean asBoolean()		{ return asBoolean(null); }
	public final World asWorld() 			{ return asWorld(null); }
	public final Server asServer() 			{ return asServer(null); }
	public final ItemStack asItemStack()	{ return asItemStack(null); }
	public final Spell asSpell() 			{ return asSpell(null); }
	public final Block asBlock() 			{ return asBlock(null); }
	public final Material asMaterial() 		{ return asMaterial(null); }
		
	public LivingEntity asLivingEntity(Context ctx) { return null; }
	public Entity asEntity(Context ctx) 		{ return null; }
	public Player asPlayer(Context ctx) 		{ return null; }
	public String asString(Context ctx) 		{ return null; }
	public Double asDouble(Context ctx) 		{ return null; }
	public Integer asInteger(Context ctx) 		{ return null; }
	public boolean asBoolean(Context ctx) 		{ return false; }	
	public World asWorld(Context ctx) 			{ return null; }
	public Server asServer(Context ctx) 		{ return null; }
	public ItemStack asItemStack(Context ctx)	{ return null; }
	public Spell asSpell(Context ctx) 			{ return null; }
	public Block asBlock(Context ctx) 			{ return null; }
	public Material asMaterial(Context ctx)		{ return null; }
	public Location asLocation(Context ctx, SelectionMode mode)	{ return null; }
	
	public <T extends Parameter> T cast(Class<T> type) {
		return cast(type, null);
	}
	
	public <T extends Parameter> T cast(Class<T> type, Context ctx) {
		if ( type.isInstance(this) ) return (T) this;
		
		if ( type.equals(EntityParameter.class) ) {
			return (T)Parameter.from(this.asEntity(ctx));
		} else if ( type.equals(PlayerParameter.class) ) {
			return (T)Parameter.from(this.asPlayer(ctx));
		} else if ( type.equals(StringParameter.class) ) {
			return (T)Parameter.from(this.asString(ctx));
		} else if ( type.equals(DoubleParameter.class) ) {
			return (T)Parameter.from(this.asDouble(ctx));
		} else if ( type.equals(IntegerParameter.class) ) {
			return (T)Parameter.from(this.asInteger(ctx));
		} else if ( type.equals(WorldParameter.class) ) {
			return (T)Parameter.from(this.asWorld(ctx));
		} else if ( type.equals(ServerParameter.class) ) {
			return (T)Parameter.from(this.asServer(ctx));
		} else if ( type.equals(ItemParameter.class) ) {
			return (T)Parameter.from(this.asItemStack(ctx));
		} else if ( type.equals(SpellParameter.class) ) {
			return (T)Parameter.from(this.asSpell(ctx));
		} else if ( type.equals(MaterialParameter.class) ) {
			return (T)Parameter.from(this.asMaterial(ctx));
		} else if ( type.equals(LocationParameter.class) ) {
			return (T)Parameter.from(this.asLocation(ctx, SelectionMode.DEFAULT));
		}
		
		return null;
	}

	public Class<? extends Parameter> getHomogeniousType() {
		return this.getClass();
	}
	
	// Factory methods
	public static Parameter from(Player p) 		{ return p == null ? null : new PlayerParameter(p); }
	public static Parameter from(Entity e) 		{ return e == null ? null : new EntityParameter(e); }
	public static Parameter from(String s) 		{ return s == null ? null : new StringParameter(s); }
	public static Parameter from(double d) 		{ return new DoubleParameter(d); }
	public static Parameter from(int i) 		{ return new IntegerParameter(i); }
	public static Parameter from(World w) 		{ return w == null ? null : new WorldParameter(w); }
	public static Parameter from(Server s) 		{ return s == null ? null : new ServerParameter(s); }
	public static Parameter from(ItemStack i) 	{ return i == null ? null : new ItemParameter(i); }
	public static Parameter from(Spell s) 		{ return s == null ? null : new SpellParameter(s); }
	public static Parameter from(boolean b)		{ return new IntegerParameter(b ? 1 : 0); }
	public static Parameter from(TCLCommand d)	{ return d == null ? null : new DelegateParameter(d); }
	public static Parameter from(Block b)	    { return b == null ? null : new BlockParameter(b); }
	public static Parameter from(Material m)	{ return m == null ? null : new MaterialParameter(m); }
	public static Parameter from(Location l)	{ return l == null ? null : new LocationParameter(l); }
	public static Parameter from(Block block, BlockFace face) {
		if ( block == null ) return null;
		if ( face == null ) return from(block);
		return new BlockParameter(block, face);
	}
	
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


	public boolean equals(Parameter other) {
		String a = this.asString();
		String b = other.asString();
		
		if ( a == null ) return false;
		if ( b == null ) return false;
		return a.equals(b);
	}

	
}
