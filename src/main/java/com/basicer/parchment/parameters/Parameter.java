package com.basicer.parchment.parameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import com.basicer.parchment.Context;
import com.basicer.parchment.Spell;
import com.basicer.parchment.TCLCommand;


public abstract class Parameter implements Iterable<Parameter> {

	//public enum SelectionMode { DEFAULT, HIT, LOOKING, STANDING };
	

	/*
	public final Location asLocation() { return asLocation(null, SelectionMode.DEFAULT); }
	public final Location asLocation(SelectionMode mode) { return asLocation(null, mode); }
	
	
	public final LivingEntity asLivingEntity() { return asLivingEntity(null); }
	public final Entity asEntity() 			{ return asEntity(null); }
	public final Player asPlayer() 			{ return asPlayer(null); }
	public final String asString() 			{ return asString(null); }
	
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
	
	public Integer asInteger(Context ctx) 		{ return null; }
	public boolean asBoolean(Context ctx) 		{ return false; }	
	public World asWorld(Context ctx) 			{ return null; }
	public Server asServer(Context ctx) 		{ return null; }
	public ItemStack asItemStack(Context ctx)	{ return null; }
	public Spell asSpell(Context ctx) 			{ return null; }
	public Block asBlock(Context ctx) 			{ return null; }
	public Material asMaterial(Context ctx)		{ return null; }
	public Location asLocation(Context ctx, SelectionMode mode)	{ return null; }
	*/
	
	public final Double asDouble() 			{ return asDouble(null); }
	public Double asDouble(Context ctx) 		{ return null; }
	
	public final String asString() 			{ return asString(null); }
	public String asString(Context ctx) 		{ return null; }
	
	public final Integer asInteger() 		{ return asInteger(null); }
	public Integer asInteger(Context ctx) 		{ return null; }
	
	public final boolean asBoolean()		{ return asBoolean(null); }
	public boolean asBoolean(Context ctx) 		{ return false; }	

	private static List<Class<? extends Parameter>> registeredTypes;

	public static void RegisterParamaterType(Class<? extends Parameter> type)
	{
		if ( registeredTypes == null ) registeredTypes = new LinkedList<Class<? extends Parameter>>();
		if ( !registeredTypes.contains(type)) registeredTypes.add(type);
		
	}

	{
		RegisterParamaterType(StringParameter.class);
		RegisterParamaterType(DelegateParameter.class);
		RegisterParamaterType(IntegerParameter.class);
		RegisterParamaterType(DoubleParameter.class);
	}

	
	public <T> T as(Class<T> type) {
		return as(type, null);
	}
	
	public <T> T as(Class<T> type, Context ctx) {
		Method nfo;
		try {
			nfo = this.getClass().getMethod("as" + type.getSimpleName(), Context.class);
		} catch ( NoSuchMethodException ex ) {
			System.out.println("Warning no conversion from " + getClass().getSimpleName() + " to " + type.getSimpleName());
			return null;
		}
		if ( nfo == null ) {
			System.out.println("Warning no conversion from " + getClass().getSimpleName() + " to " + type.getSimpleName());
			return null;
		}
		Object r;
		try {
			r = nfo.invoke(this, ctx);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		if ( type.isInstance(r) ) return (T) r;
		return null;
	}
	
	public <T extends Parameter> T cast(Class<T> type) {
		return cast(type, null);
	}
	
	public Parameter castByString(String name) {
		Class<? extends Parameter> type = classForName(name);
		return cast(type);
	}
	
	public Parameter castByString(String name, Context ctx) {
		Class<? extends Parameter> type = classForName(name);
		return cast(type, ctx);
	}
	
	private static Class<? extends Parameter> classForName(String s) {
		
		if ( registeredTypes == null ) return null;
		String match = s + "Parameter";
		for ( Class<? extends Parameter> c : registeredTypes ) {
			if ( c.getSimpleName().equals(match) ) return c;
		}
		return null;
	}
	
	public abstract Class getUnderlyingType();
	
	public Object getUnderlyingValue() {
		Field f;
		try {
			f = this.getClass().getDeclaredField("self");
			f.setAccessible(true);
			return f.get(this);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}
	
	public <T extends Parameter> T cast(Class<T> type, Context ctx) {
		if ( type.isInstance(this) ) return (T) this;
		
		Method m;
		
		try {
			System.out.println("Trying Static Caster | " + type + " from " + this.getClass());
			m = type.getDeclaredMethod("castFrom", this.getClass(), Context.class);
			System.out.println("I survived");
			//System.out.println("Using Static Caster" + type);
			T out = (T)m.invoke(null, this, ctx);
			System.out.println("Using Static Caster" + type + " | " + out);
			return out;
		} catch ( Exception ex ) {
			ex.printStackTrace(System.out);
		}
		
		System.out.println("Trying COnstrucotr Caster" + type);
		
		try {
			System.out.println("Har we go casting " + this + " to " + type);
			Class<?> oc = type.getDeclaredField("self").getType();
			Object o = this.as(oc);
			if ( o == null ) {
				return null;
			}
			
			Constructor<?> con = type.getDeclaredConstructor(oc);
			System.out.println("We are " + getClass().getSimpleName() + " : " + this.asString());
			System.out.println("Use " + con.toString() + " with param type " + oc);
			con.setAccessible(true);
			
			return (T) con.newInstance(o);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	
	}

	public Class<? extends Parameter> getHomogeniousType() {
		return this.getClass();
	}
	
	
	// Factory methods
	
	public static StringParameter 	from(String s) 		{ return s == null ? null : new StringParameter(s); }
	public static DoubleParameter 	from(double d) 		{ return new DoubleParameter(d); }
	public static DoubleParameter 	from(Double d)		{ return d == null ? null : new DoubleParameter(d); }
	public static IntegerParameter 	from(int i) 		{ return new IntegerParameter(i); }
	public static IntegerParameter 	from(Integer i)		{ return i == null ? null : new IntegerParameter(i); }
	public static IntegerParameter 	from(boolean b)		{ return new IntegerParameter(b ? 1 : 0); }
	
	
/*
	public static PlayerParameter 	from(Player p) 		{ return p == null ? null : new PlayerParameter(p); }
	public static EntityParameter 	from(Entity e) 		{ return e == null ? null : new EntityParameter(e); }
	public static LivingEntityParameter from(LivingEntity e) { return e == null ? null : new LivingEntityParameter(e); }
	public static WorldParameter 	from(World w) 		{ return w == null ? null : new WorldParameter(w); }
	public static ServerParameter 	from(Server s) 		{ return s == null ? null : new ServerParameter(s); }
	public static ItemParameter 	from(ItemStack i) 	{ return i == null ? null : new ItemParameter(i); }
	public static SpellParameter 	from(Spell s) 		{ return s == null ? null : new SpellParameter(s); }
	
	
	public static BlockParameter 	from(Block b)	    { return b == null ? null : new BlockParameter(b); }
	public static MaterialParameter from(Material m)	{ return m == null ? null : new MaterialParameter(m); }
	public static LocationParameter from(Location l)	{ return l == null ? null : new LocationParameter(l); }

*/
	
	private static Parameter tryParamCast(Object o, Class<? extends Parameter> type, boolean strict)
	{
		Class<?> xtype = o.getClass();
		try {
			Constructor[] cons = type.getDeclaredConstructors();
			for ( Constructor con : cons ) {
				Class[] types = con.getParameterTypes();
				if ( types.length != 1 ) return null;
				if ( strict ) {
					if ( types[0] != type ) return null;
				} else { 
					if ( !types[0].isAssignableFrom(xtype) ) return null;
				}
				con.setAccessible(true);
				return (Parameter) con.newInstance(o);
			}
			return null;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	
	}
	
	public static Parameter from(Object o)
	{
		Class type = o.getClass();
		if ( registeredTypes == null ) return null;
		//Todo: Cache this and sort it.
		for ( Class c : registeredTypes ) {
			Parameter result = tryParamCast(o, c, true);
			if ( result != null ) return result;
		}

		System.err.println("Uh, so that wasnt any good, lets try this.");
		for ( Class c2 : registeredTypes ) {
			Parameter result = tryParamCast(o, c2, false);
			if ( result != null ) return result;
		}
		
		throw new RuntimeException("Failed cast on " + type.getSimpleName());

	}
	
	public static Parameter from(Object o, Object... args ) {
		System.out.println("WARNING| IMPLEMENT EXTRA ARGUEMENTS");
		return from(o);
	}
	
	public static Parameter fromObject(Object data) {
		Class datatype = data.getClass();
		
		if ( datatype == Short.class ) 			return Parameter.from(((Short)data).intValue());
		else if ( datatype == String.class ) 	return Parameter.from(((String)data).toString());
		else if ( datatype == Integer.class )	return Parameter.from((Integer) data);
		else if ( datatype == Double.class ) 	return Parameter.from((Double) data);
		else if ( datatype == Float.class ) 	return Parameter.from(((Float) data).doubleValue());
		else if ( datatype == Boolean.class ) 	return Parameter.from(((Boolean) data).booleanValue());
		
		return Parameter.from(data.toString());
		 
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

	public Parameter get(String string) {
		return Parameter.from("[GET: " + string + "]");
	}

	
	@Override
	public boolean equals(Object oother) {
		if ( oother instanceof Parameter ) {
			Parameter other = (Parameter) oother;
			Double da = this.asDouble();
			Double db = other.asDouble();

			if ( da != null && db != null ) return da.equals(db);
			
			String a = this.asString();
			String b = other.asString();
			
			if ( a == null ) return false;
			if ( b == null ) return false;
			
			
			return a.equals(b);
		} return false;
	}
	
	@Override
	public int hashCode() {
		return this.asString().hashCode();
	}
	

	
}
