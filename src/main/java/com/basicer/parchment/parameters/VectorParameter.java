package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import org.bukkit.util.Vector;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class VectorParameter extends Parameter {

	@SuppressWarnings("rawtypes")
	@Override
	public Class<Vector> getUnderlyingType() { return Vector.class; }

	private Vector self;

	VectorParameter(Vector self) {
		this.self = self.clone();
	}
	


/*	
	public LivingEntity asLivingEntity(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.as(LivingEntity.class);
	}

	
	public Entity asEntity(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.as(Entity.class);
	}

	
	public Player asPlayer(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.as(Player.class);
	}
	
	
	public Block asBlock(Context ctx) {
		if ( self.size() != 1 ) return null;
		Parameter p = self.get(0);
		if ( p == null ) return null;
		return p.as(Block.class);
	}

	*/

	public static VectorParameter from(Vector self) {
		return new VectorParameter(self);
	}

	public static VectorParameter castFrom(LocationParameter l, Context ctx) {
		return new VectorParameter(l.asLocation(ctx).toVector());
	}

	public static VectorParameter castFrom(BlockParameter l, Context ctx) {
		return new VectorParameter(l.asLocation(ctx).toVector());
	}


	public Vector asVector() { return self; }
	public Vector asVector(Context ctx) { return self; }

	public ListParameter asList(Context ctx) {
		return vector2List(self);
	}

	@Override
	public String asString(Context ctx) {
		StringBuilder b = new StringBuilder();
		b.append(self.getX());
		b.append(" ");
		b.append(self.getY());
		b.append(" ");
		b.append(self.getZ());
		return b.toString();
	}


	@Override
	public Parameter index(String n) {
		try {
			int id = Integer.parseInt(n);
			return  index(id);
		} catch ( NumberFormatException ex ) {
			throw new FizzleException("Must index ListParameter with integer");
		}

	}

	@Override
	public Parameter index(int n) {
		switch ( n ) {
			case 0: return DoubleParameter.from(self.getX());
			case 1: return DoubleParameter.from(self.getY());
			case 2: return DoubleParameter.from(self.getZ());
			default: return null;
		}
	}

	public static VectorParameter castFrom(StringParameter in, Context ctx) {
		ListParameter lp = ListParameter.castFrom(in, ctx);
		if ( lp == null ) return null;
		Vector v = list2Vector(lp);
		if ( v == null ) return null;
		return new VectorParameter(v);
	}

	private static Vector list2Vector(ListParameter v) {
		Vector out = new Vector();
		if ( v.length() == 3 ) {
			out.setX(v.index(0).asDouble());
			out.setY(v.index(1).asDouble());
			out.setZ(v.index(2).asDouble());
			return out;
		} else if ( v.length() == 2 ) {
			out.setX(v.index(0).asDouble());
			out.setZ(v.index(1).asDouble());
		} else {
			out.setY(v.index(0).asDouble());
		}
		return null;
	}

	private static ListParameter vector2List(Vector v) {
		ListParameter out = ListParameter.createEmpty();
		out.add(DoubleParameter.from(v.getX()));
		out.add(DoubleParameter.from(v.getY()));
		out.add(DoubleParameter.from(v.getZ()));
		return out;
	}


	public boolean isArray() { return true; }

	@Override
	public Parameter cloneIfMutable() { return this; }

	public int length() {
		return 3;
	}
	

}
