package com.basicer.parchment;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import com.basicer.parchment.base.*;
import com.basicer.parchment.spells.*;
import com.basicer.parchment.tcl.*;
import com.basicer.parchment.tcl.Error;
import com.basicer.parchment.test.Test;

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
		addBuiltinCommand(Format.class);
		addBuiltinCommand(Proc.class);
		addBuiltinCommand(Expr.class);
		addBuiltinCommand(If.class);
		addBuiltinCommand(Eval.class);
		addBuiltinCommand(Uplevel.class);
		addBuiltinCommand(While.class);
		addBuiltinCommand(For.class);
		addBuiltinCommand(Return.class);
		addBuiltinCommand(Break.class);
		addBuiltinCommand(Error.class);
		addBuiltinCommand(Type.class);
		addBuiltinCommand(Incr.class);
		addBuiltinCommand(Static.class);
		addBuiltinCommand(Global.class);
		addBuiltinCommand(Catch.class);
		addBuiltinCommand(Unset.class);
		
		addBuiltinCommand(Import.class);
		addBuiltinCommand(Join.class);
		
		addBuiltinCommand(Test.class);
		addBuiltinCommand(List.class);
		
		addBuiltinCommand(StringCmd.class);
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
		addBuiltinSpell(Player.class);
		addBuiltinSpell(Server.class);

	}
	
	public void addCustomSpell(String name, ScriptedSpell spell) {
		commands.put(name, spell);
	}
	
	public <T extends Spell> void addBuiltinSpell(Class<T> spell) {
		try {
			TCLCommand s = spell.newInstance();
			commands.put(s.getName(), s);
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}
	
	public <T extends TCLCommand> void addBuiltinCommand(Class<T> cmd) {
		try {
			TCLCommand s = cmd.newInstance();
			commands.put(s.getName(), s);
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}

	public Dictionary<String, TCLCommand> getAll() {
		return commands;
	}
}
