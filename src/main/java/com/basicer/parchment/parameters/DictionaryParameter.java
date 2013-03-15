package com.basicer.parchment.parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public Parameter index(String s) {
		return self.get(s);
	}
	
	public void writeIndex(String string, Parameter val) {
		self.put(string, val);
	}
	
	public String asString(Context ctx) { 
		return "[Dictionary]";
	}
	
}