package com.basicer.parchment;

import com.basicer.parchment.parameters.Parameter;

public class SpellContext {
	
	private Parameter target;
	private Parameter caster;
	
	public Parameter getCaster() {
		return caster;
	}

	public void setCaster(Parameter caster) {
		this.caster = caster;
	}

	public Parameter getTarget() {
		return target;
	}

	public void setTarget(Parameter target) {
		this.target = target;
	}
	
	public void sendDebugMessage(String msg) {
		caster.asPlayer().sendMessage(msg);
	}
	
}
