package com.basicer.parchment.parameters;

import java.io.PushbackReader;
import java.io.StringReader;
import java.util.*;


import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;


public class ListParameter extends Parameter {

	@SuppressWarnings("rawtypes")
	@Override
	public Class<List> getUnderlyingType() { return List.class; }
	
	private List<Parameter> self;

	ListParameter(List<Parameter> self) {
		this.self = new ArrayList<Parameter>(self);;
	}
	

	public Class<? extends Parameter> getHomogeniousType() {
		Class<? extends Parameter> out = null;
		for ( Parameter p : self ) {
			Class<? extends Parameter> x = p.getClass();
			if ( out == null ) out = x;
			else if ( out.equals(x) ) continue;
			else {
				out = null;
				break;
			}
		}
		return out;
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

	public static ListParameter createEmpty() {
		return new ListParameter(new ArrayList<Parameter>());
	}

	@Override
	public String asString(Context ctx) {
		StringBuilder b = new StringBuilder();
		boolean first = true;
		for ( Parameter p : self) {
			if ( b.length() > 0 ) b.append(" ");
			/*
			String x = p.asString();
			if ( x == null || x.length() < 1 ) b.append("{}");
			else if ( x.contains(" ") || x.contains("\\") ) {
				b.append('{');
				b.append(x);
				b.append('}');
			} else {
				b.append(x);
			}
			*/
			if ( p != null ) {
				b.append(com.basicer.parchment.tcl.List.encode(p.asString(), first));
			} else b.append("{}");
			first = false;
		}
		return b.toString();
	}

	@Override
	public Iterator<Parameter> iterator() {
		return self.iterator();
	}
	
	public ArrayList<Parameter> asArrayList() {
		return new ArrayList<Parameter>(self);
	}

	public static ListParameter from(ArrayList<Parameter> self) {
		return new ListParameter(self);
	}

	public static ListParameter from(LinkedList<Parameter> self) {
		return new ListParameter(new ArrayList(self));
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
		return self.get(n);
	}

	public static ListParameter castFrom(StringParameter in, Context ctx) {
		Debug.info("Making list");
		ParameterAccumulator[] tkns = TCLEngine.parseLine(new PushbackReader(new StringReader(in.asString(ctx)), 2), null);
		ArrayList<Parameter> out = new ArrayList<Parameter>();
		for ( ParameterAccumulator p : tkns ) {
			out.add(p.cheatyResolveOrFizzle());
		}
		return from(out);
	}

	public boolean isArray() { return true; }

	@Override
	public Parameter cloneIfMutable() { return new ListParameter(self); }


	public void add(Parameter parameter) {
		self.add(parameter);
	}


	public int length() {
		return self.size();
	}
	

}
