package com.basicer.parchment;

import java.util.HashMap;
import java.util.Map;

import com.basicer.parchment.parameters.Parameter;

public class Context {
	
	private Context parent;
	
	private Map<String, ParameterPtr> variables;
	
	public Context() { 
		variables = new HashMap<String, ParameterPtr>();
		
	}
	
	private Context(Context parent) {
		this.parent = parent;
	}
	
	public void push() {
		Context new_parent = new Context(parent);
		Map<String, ParameterPtr> swap = new_parent.variables;
		new_parent.variables = variables;
		variables = swap;
	}
	
	public Parameter get(String var) {
		ParameterPtr ptr = variables.get(var);
		if ( ptr == null ) return null;
		return ptr.val;
	}
	
	public void put(String var, Parameter value) {
		if ( variables.containsKey(var) ) {
			getRaw(var).val = value;
		} else {
			ParameterPtr nv = new ParameterPtr();
			nv.val = value;
			variables.put(var, nv);
		}
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
	
	public Context createSubContext() {
		Context ctx = new Context();
		ctx.parent = this;
		return ctx;
	}
	
	private Context up(int level) {
		Context out = this;
		for ( int i = 0; i < level; ++i ) {
			out = out.parent;
		}
		return out;
	}
	
	public void upvar(int level, String var) {
		Context ctx = up(level);
		setRaw(var, ctx.getRaw(var));
	}
	
	
	private ParameterPtr getRaw(String var) {
		return variables.get(var);
	}
	
	private void setRaw(String var, ParameterPtr p) {
		variables.put(var, p);
	}
	
	private class ParameterPtr {
		public Parameter val;
	}
	
}
