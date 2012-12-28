package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;

public class DelegateParameter extends Parameter {


	private TCLCommand self;
	
	DelegateParameter(TCLCommand self) {
		this.self = self;
	}
	
	public Parameter evaluate(Context ctx) {
		return self.execute(ctx);
	}
}
