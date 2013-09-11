package com.basicer.parchment.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.meta.ItemMeta;

import com.basicer.parchment.Context;

public class ArrayParameter extends Parameter {

	@Override
	public Class getUnderlyingType() { return Map.class; }

	private Map<String, Parameter> self;
	ArrayParameter(Map<String, Parameter> self) {
		this.self = self;
	}
	
	public ArrayParameter() {
		this.self = new HashMap<String, Parameter>();
	}
	
	public Parameter index(String s) {
		return self.get(s);
	}
	
	public void writeIndex(String string, Parameter val) {
		self.put(string, val);
	}
	
	public void deleteIndex(String name) {
		self.remove(name);
	}
	
	public String asString(Context ctx) { 
		return "[Dictionary: " + self.toString() + "]";
	}
	
	public boolean isArray() { return true; }

	public boolean hasIndex(String name) {
		return self.containsKey(name);
	}
}
