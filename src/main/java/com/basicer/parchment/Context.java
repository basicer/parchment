package com.basicer.parchment;

import java.util.HashMap;
import java.util.Map;

import com.basicer.parchment.parameters.Parameter;

public class Context {
	
	private Context parent;
	
	private Map<String, Parameter> variables;
	
	public Context() { 
		variables = new HashMap<String, Parameter>();
		
	}
	
	private Context(Context parent) {
		this.parent = parent;
	}
	
	public void push() {
		Context new_parent = new Context(parent);
		Map<String, Parameter> swap = new_parent.variables;
		new_parent.variables = variables;
		variables = swap;
	}
	
	public Parameter get(String var) {
		return variables.get(var);
	}
	
	public void put(String var, Parameter value) {
		variables.put(var, value);
	}
	
	public void pop() {
		if ( parent == null ) throw new IllegalStateException("Can't pop base context.");
		this.variables = parent.variables;
	}
	
	public Parameter getCaster() {
		Parameter caster = get("caster");
		if ( caster == null && parent != null ) return parent.getCaster();
		return caster;
	}

	public void setCaster(Parameter caster) {
		put("caster", caster);
	}

	public Parameter getTarget() {
		Parameter target = get("target");
		if ( target == null && parent != null ) return parent.getTarget();
		return target;
	}

	public void setTarget(Parameter target) {
		put("target", target);
	}
	
	public void sendDebugMessage(String msg) {
		getCaster().asPlayer().sendMessage(msg);
	}
	
}
