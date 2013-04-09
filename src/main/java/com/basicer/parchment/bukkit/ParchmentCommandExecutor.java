package com.basicer.parchment.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class ParchmentCommandExecutor implements CommandExecutor {

	private ParchmentPlugin plugin;
	private Context	commandctx;  //TODO: Replace with per player ctx
	
	public ParchmentCommandExecutor(ParchmentPlugin plugin) {
		this.plugin = plugin;
		commandctx = new Context(); 
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!sender.isOp())
			return false;

		Queue<String> qargs = new LinkedList<String>(Arrays.asList(args));
		String action = label;

		final Context ctx = commandctx.createSubContext();
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ctx.setSpellFactory(plugin.getSpellFactory());
			ctx.setCaster(Parameter.from(p));
			ctx.setWorld(Parameter.from(p.getWorld()));
			ctx.setServer(Parameter.from(p.getServer()));
			ctx.setSource("command");
		} else {
			return false;
		}
		
		if ( label.equals("scriptmode") ) {
			if (!(sender instanceof Player)) return false;
			final Player p = (Player) sender;
			
			p.beginConversation(new Conversation(plugin, p, new Prompt() {

				public Prompt acceptInput(ConversationContext arg0, String arg1) {

					if ( arg1.startsWith("/") ) arg1 = arg1.substring(1);
					if ( arg1.equals("exit") ) return null;
					Parameter r = null;
					try { r = TCLUtils.evaluate(arg1, ctx); }
					catch ( Exception ex ) {
						
					} catch ( Error ex ) {
						
					}
					if ( r != null ) ctx.put("ans", r);
					else ctx.put("ans", Parameter.from(""));
					return this;
				}

				public boolean blocksForInput(ConversationContext arg0) {
					return true;
				}

				public String getPromptText(ConversationContext arg0) {
					return "TCL>";
				}
				
			}));
			return true;
		}
		
		if (label.equals("parchment") || label.equals("p")) {
			action = qargs.poll();
		}

		if (action == null) return false;
		


		if (action.equals("cast") || action.equals("c")) {
			StringBuilder b = null;
			while (!qargs.isEmpty()) {
				if (b == null)
					b = new StringBuilder();
				else
					b.append(" ");
				b.append(qargs.poll());
			}

			//Todo: Show a prettyer error.
			if ( b == null ) return false;
			TCLEngine e = new TCLEngine(b.toString(), ctx);
			while ( e.step() ) {}
			if ( e.getCode() == Code.ERROR ) {
				sender.sendMessage(ChatColor.RED +"Error: " + e.getResult().asString());
			}
			
		} else if (action.equals("run")) {
			String file = qargs.poll() + ".tcl";
			File folder = FSUtils.findOrCreateDirectory(plugin.getDataFolder(), "runnable");
			File rfile = FSUtils.findFile(folder, file);
			if (rfile == null) {
				sender.sendMessage("Unknown file " + file);
				return true;
			}
			PushbackReader reader;
			try {
				reader = new PushbackReader(new InputStreamReader(new FileInputStream(rfile)));
				TCLUtils.evaluate(reader, ctx);
			} catch (FileNotFoundException e) {
				sender.sendMessage("Unknown file 2 " + file);
			}

		} else {
			sender.sendMessage("Unknonw action " + action);
		}

		return true;
	}


}
