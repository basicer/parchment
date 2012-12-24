package com.basicer.parchment;

import java.util.Dictionary;
import java.util.Hashtable;

import com.basicer.parchment.base.Color;
import com.basicer.parchment.base.Item;
import com.basicer.parchment.base.SCommand;
import com.basicer.parchment.spells.*;
import com.basicer.parchment.tcl.*;

public class SpellFactory {
	
	private static SpellFactory _instance;
	public static SpellFactory instance() {
		if ( _instance == null ) _instance = new SpellFactory();
		return _instance;
	}
	
	public static TCLCommand get(String name) {
		return instance().commands.get(name.toLowerCase());
	}
	
	Dictionary<String, TCLCommand> commands;
	private SpellFactory() {
		commands = new Hashtable<String,TCLCommand>();
		
		addBuiltinCommand(PutS.class);
		addBuiltinCommand(Set.class);
		addBuiltinCommand(Concat.class);
		addBuiltinCommand(SCommand.class);
		addBuiltinCommand(Color.class);
		
		addBuiltinSpell(Item.class);
		addBuiltinSpell(Heal.class);
		addBuiltinSpell(Shoot.class);
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
