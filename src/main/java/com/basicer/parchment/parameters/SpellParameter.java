package com.basicer.parchment.parameters;

import com.basicer.parchment.Spell;

public class SpellParameter extends Parameter {
	private Spell self;
	SpellParameter(Spell self) {
		this.self = self;
	}
	
	public Spell asSpell() { return self; }
	
}
