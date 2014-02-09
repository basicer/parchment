package com.basicer.parchment;

import java.util.*;

import com.basicer.parchment.base.*;
import com.basicer.parchment.extra.IMenu;
import com.basicer.parchment.spells.*;
import com.basicer.parchment.tcl.*;
import com.basicer.parchment.tcl.Error;
import com.basicer.parchment.tcl.List;
import com.basicer.parchment.tcl.Set;
import com.basicer.parchment.tclutil.*;
import com.basicer.parchment.test.Test;
import com.basicer.parchment.test.TestConstraint;

public class SpellFactory {
	
	public TCLCommand get(String name) {
		return commands.get(name.toLowerCase());
	}
	
	public int getScriptCommandCount() {
		int count = 0;
		Enumeration<TCLCommand> en = commands.elements();
		while ( en.hasMoreElements() ) {
			if ( en.nextElement() instanceof ScriptedSpell ) ++count;
		}
		return count;
	}

	Hashtable<String, TCLCommand> commands;
	ArrayList<ScriptedSpell> ss_cache;

	public SpellFactory() {
		commands = new Hashtable<String,TCLCommand>();
		ss_cache = new ArrayList<ScriptedSpell>();
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
		addBuiltinCommand(Continue.class);
		addBuiltinCommand(Error.class);
		addBuiltinCommand(Type.class);
		addBuiltinCommand(Incr.class);
		addBuiltinCommand(Info.class);
		addBuiltinCommand(Append.class);
		addBuiltinCommand(Static.class);
		addBuiltinCommand(Global.class);
		addBuiltinCommand(Catch.class);
		addBuiltinCommand(Unset.class);
		
		addBuiltinCommand(Import.class);
		addBuiltinCommand(Join.class);
		
		addBuiltinCommand(Test.class);
		addBuiltinCommand(TestConstraint.class);

		addBuiltinCommand(Array.class);
		addBuiltinCommand(List.class);
		addBuiltinCommand(Dict.class);
		
		addBuiltinCommand(StringCmd.class);
		
		addBuiltinCommand(LIndex.class);
		addBuiltinCommand(LAppend.class);
		addBuiltinCommand(LLength.class);
		addBuiltinCommand(LShuffle.class);
		addBuiltinCommand(LSearch.class);

		addBuiltinCommand(Clock.class);


	}
	
	public void load() {
		
		
		loadTCLOnly();
		
		addBuiltinCommand(ET.class);
		
		addBuiltinCommand(After.class);
		addBuiltinCommand(Dist.class);
		addBuiltinCommand(Hash.class);
		addBuiltinCommand(With.class);
		addBuiltinCommand(Http.class);
		
		addBuiltinCommand(Expand.class);
		addBuiltinCommand(SCommand.class);
		addBuiltinCommand(PCommand.class);
		addBuiltinCommand(Color.class);
		addBuiltinCommand(Bind.class);
		addBuiltinCommand(Metadata.class);
		addBuiltinCommand(Find.class);

		addBuiltinCommand(WGRegion.class);
		addBuiltinCommand(Command.class);
		addBuiltinCommand(Bungee.class);

		addBuiltinCommand(IMenu.class);

		addBuiltinCommand(Repeat.class);
		
		addBuiltinSpell(Item.class);
		addBuiltinSpell(Block.class);
		addBuiltinSpell(Heal.class);
		addBuiltinSpell(Shoot.class);
		addBuiltinSpell(Explode.class);
		addBuiltinSpell(Entity.class);
		addBuiltinSpell(LEntity.class);
		addBuiltinSpell(Player.class);
		addBuiltinSpell(Firework.class);
		addBuiltinSpell(Server.class);
		addBuiltinSpell(World.class);

		addBuiltinSpell(Disguise.class);

	}
	
	public void addCustomSpell(String name, ScriptedSpell spell) {
		put(name, spell);
	}

	private void put(String name, TCLCommand s) {
		if ( s instanceof ScriptedSpell ) {
			if ( commands.containsKey(name) ) {
				ss_cache.remove(commands.get(name));
			}
			ss_cache.add((ScriptedSpell) s);
		}
		commands.put(name, s);

	}

	public <T extends Spell> void addBuiltinSpell(Class<T> spell) {
		try {
			TCLCommand s = spell.newInstance();
			if ( !s.supportedByServer() ) return;
			put(s.getName(), s);
			for ( String a : s.getAliases() ) put(a,s);
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}
	
	public <T extends TCLCommand> void addBuiltinCommand(Class<T> cmd) {
		try {
			TCLCommand s = cmd.newInstance();
			if ( !s.supportedByServer() ) return;
			put(s.getName(), s);
			for ( String a : s.getAliases() ) put(a,s);
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		}
	}

	public Hashtable<String, TCLCommand> getAll() {
		return commands;
	}

	public Enumeration<ScriptedSpell> findAllWithBinding(String binding) {
		ArrayList<ScriptedSpell> list = new ArrayList<ScriptedSpell>();

		for ( ScriptedSpell s : ss_cache ) {
			if ( !s.canExecuteBinding(binding) ) continue;
			list.add(s);
		}
		return Collections.enumeration(list);
	}
}
