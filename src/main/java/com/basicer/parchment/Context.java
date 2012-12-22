package com.basicer.parchment;

import com.basicer.parchment.parameters.Parameter;

public class Context {
	
	private Context parent;
	
	private Parameter target;
	private Parameter caster;
	
	public Context() { }
	
	private Context(Context parent) {
		this.parent = parent;
	}
	
	public void push() {
		Context new_parent = new Context(parent);
		new_parent.target = target;
		new_parent.caster = caster;
		
		parent = new_parent;
		target = null;
		caster = null;
	}
	
	public void pop() {
		if ( parent == null ) throw new IllegalStateException("Can't pop base context.");
		this.target = parent.target;
		this.caster = parent.caster;
		this.parent = parent.parent;
	}
	
	public Parameter getCaster() {
		if ( target == null && caster != null ) return parent.getCaster();
		return caster;
	}

	public void setCaster(Parameter caster) {
		this.caster = caster;
	}

	public Parameter getTarget() {
		if ( target == null && parent != null ) return parent.getTarget();
		return target;
	}

	public void setTarget(Parameter target) {
		this.target = target;
	}
	
	public void sendDebugMessage(String msg) {
		caster.asPlayer().sendMessage(msg);
	}
	
}
