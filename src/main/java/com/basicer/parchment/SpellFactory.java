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
	
	public static Spell get(String name) {
		return instance().spells.get(name.toLowerCase());
	}
	
	Dictionary<String, Spell> spells;
	private SpellFactory() {
		spells = new Hashtable<String,Spell>();
		addBuiltinSpell(Heal.class);
		addBuiltinSpell(Shoot.class);
	}
	
	private <T extends Spell> void addBuiltinSpell(Class<T> spell) {
		try {
			spells.put(spell.getSimpleName().toLowerCase(), spell.newInstance());
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}
}
