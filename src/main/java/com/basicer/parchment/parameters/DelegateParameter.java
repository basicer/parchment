package com.basicer.parchment.parameters;

import org.bukkit.block.Block;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;

public class DelegateParameter extends Parameter {


	private TCLCommand self;
	
	
	DelegateParameter(TCLCommand self) {
		this.self = self;
	}
	
	@Override
	public Class getUnderlyingType() { return TCLCommand.class; }
	
	public Parameter evaluate(Context ctx) {
		return self.execute(ctx);
	}
}
