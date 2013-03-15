package com.basicer.parchment.parameters;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;

public class DelegateParameter extends Parameter {


	private TCLCommand self;
	
	
	DelegateParameter(TCLCommand self) {
		this.self = self;
	}
	
	@Override
	public Class<TCLCommand> getUnderlyingType() { return TCLCommand.class; }
	
	public Parameter evaluate(Context ctx) {
		return self.extendedExecute(ctx, null).getValue();
	}
	
	public TCLCommand asTCLCommand(Context ctx) {
		return self;
	}
	
	public String asString(Context ctx) { return self.toString(); }
}
