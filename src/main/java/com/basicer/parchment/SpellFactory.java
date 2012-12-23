package com.basicer.parchment;

import java.util.Dictionary;
import java.util.Hashtable;

import com.basicer.parchment.spells.*;

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
