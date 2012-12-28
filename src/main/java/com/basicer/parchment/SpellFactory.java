package com.basicer.parchment;

import java.util.Dictionary;
import java.util.Hashtable;

import com.basicer.parchment.base.*;
import com.basicer.parchment.spells.*;
import com.basicer.parchment.tcl.*;

public class SpellFactory {
	
	public TCLCommand get(String name) {
		return commands.get(name.toLowerCase());
	}
	
	Dictionary<String, TCLCommand> commands;
	public SpellFactory() {
		commands = new Hashtable<String,TCLCommand>();
	}
	
	public void load() {
		
		
		addBuiltinCommand(PutS.class);
		addBuiltinCommand(Set.class);
		addBuiltinCommand(Concat.class);
		addBuiltinCommand(SCommand.class);
		addBuiltinCommand(PCommand.class);
		addBuiltinCommand(Color.class);
		addBuiltinCommand(Bind.class);
		addBuiltinCommand(Upvar.class);
		addBuiltinCommand(Expand.class);
		
		addBuiltinCommand(Proc.class);
		addBuiltinCommand(Expr.class);
		addBuiltinCommand(If.class);
		
		addBuiltinSpell(Item.class);
		addBuiltinSpell(Block.class);
		addBuiltinSpell(Heal.class);
		addBuiltinSpell(Shoot.class);
		
	}
	
	public void addCustomSpell(String name, ScriptedSpell spell) {
		commands.put(name, spell);
	}
	
	private <T extends Spell> void addBuiltinSpell(Class<T> spell) {
		try {
			commands.put(spell.getSimpleName().toLowerCase(), spell.newInstance());
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}
	
	private <T extends TCLCommand> void addBuiltinCommand(Class<T> cmd) {
		try {
			commands.put(cmd.getSimpleName().toLowerCase(), cmd.newInstance());
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}
}
