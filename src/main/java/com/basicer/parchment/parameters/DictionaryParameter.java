package com.basicer.parchment.parameters;

import java.util.*;

import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;

public class DictionaryParameter extends Parameter {

	@Override
	public Class getUnderlyingType() { return Map.class; }

	private Map<String, Parameter> self;
	DictionaryParameter(Map<String, Parameter> self) {
		this.self = self;
	}
	
	public DictionaryParameter() {
		this.self = new HashMap<String, Parameter>();
	}

	@Override
	public Parameter index(int i) {
		return index("" +i );
	}

	@Override
	public Parameter index(String s) {
		return self.get(s);
	}
	
	public void writeIndex(String string, Parameter val) {
		self.put(string, val);
	}
	
	public void deleteIndex(String name) {
		self.remove(name);
	}

	/*
	 * We sorta treat arrays and Dicts the same way right now.  It's kinda
	 * freaky, but it sure is handy.
	 */
	public String asString(Context ctx) {
		return asList(ctx).asString(ctx);
	}

	public ListParameter asList(Context ctx) {
		ArrayList<Parameter> pairs = new ArrayList<Parameter>();
		for ( String s : self.keySet() ) {
			pairs.add(Parameter.from(s));
			pairs.add(self.get(s));
		}

		return ListParameter.from(pairs);
	}

	public static DictionaryParameter castFrom(ListParameter in, Context ctx) {
		if ( in.length() % 2 == 1 ) return null;
		DictionaryParameter out = new DictionaryParameter();
		for ( int i = 0; i < in.length(); i += 2 ) {
			out.writeIndex(in.index(i).asString(ctx), in.index(i+1));
		}
		return out;
	}

	public boolean isArray() { return true; }

	public boolean hasIndex(String name) {
		return self.containsKey(name);
	}

	public int size() {
		return self.size();
	}

	public Set<String> getGetSet() {
		return self.keySet();
	}
}
