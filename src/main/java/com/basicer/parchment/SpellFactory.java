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
	
	public void loadTCLOnly() {
		addBuiltinCommand(PutS.class);
		addBuiltinCommand(Set.class);
		addBuiltinCommand(Concat.class);

		addBuiltinCommand(Upvar.class);
		
		addBuiltinCommand(Proc.class);
		addBuiltinCommand(Expr.class);
		addBuiltinCommand(If.class);
		addBuiltinCommand(While.class);
		addBuiltinCommand(Return.class);
		addBuiltinCommand(Break.class);
		addBuiltinCommand(Cast.class);
	}
	
	public void load() {
		
		
		loadTCLOnly();
		addBuiltinCommand(Expand.class);
		addBuiltinCommand(SCommand.class);
		addBuiltinCommand(PCommand.class);
		addBuiltinCommand(Color.class);
		addBuiltinCommand(Bind.class);
		

		
		addBuiltinSpell(Item.class);
		addBuiltinSpell(Block.class);
		addBuiltinSpell(Heal.class);
		addBuiltinSpell(Shoot.class);
		addBuiltinSpell(Explode.class);
		addBuiltinSpell(Entity.class);
		
		addBuiltinSpell(Spout.class);
		
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
