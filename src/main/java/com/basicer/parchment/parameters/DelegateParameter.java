package com.basicer.parchment.parameters;

public class DelegateParameter extends Parameter {


	private Delegate self;
	
	DelegateParameter(Delegate self) {
		this.self = self;
	}
	
	public Parameter invoke() {
		return self.invoke();
	}
}
