package com.basicer.parchment.bukkit;

import java.io.*;
import java.util.*;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.StringParameter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class ParchmentCommandExecutor implements CommandExecutor, TabCompleter {

	private ParchmentPluginLite plugin;
	private Context	commandctx;  //TODO: Replace with per player ctx

	public ParchmentCommandExecutor(ParchmentPluginLite plugin) {
		this.plugin = plugin;
		commandctx = new Context();
		commandctx.setSpellFactory(plugin.getSpellFactory());

	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
		switch ( command.getName() ) {
			case "tcl":
				StringBuilder b = new StringBuilder();
				for ( String s : args ) {
					if ( b.length() > 0 ) b.append(" ");
					b.append(s);
				}

				List<String> list = TCLUtils.tabComplete(b.toString(), getContext(commandSender));
				if ( list == null ) return null;
				return list;
			default:
				return null;
		}
	}

	private static class ScriptModePrompt implements Prompt {
		private ParchmentPluginLite plugin = null;
		private Context ctx = null;
		private StringBuilder buffer;
		public ScriptModePrompt(ParchmentPluginLite plugin, Context ctx) {
			this.plugin = plugin;
			this.buffer = new StringBuilder();
			this.ctx = ctx;
		}

		@Override
		public Prompt acceptInput(final ConversationContext arg0, String arg1) {

			if ( arg1.startsWith("/") ) arg1 = arg1.substring(1);
			if ( arg1.equals("exit") || arg1.equals("quit") || arg1.equals("stop") ) return null;

			Parameter r = null;
			buffer.append(arg1);
			buffer.append("\n");

			String test = buffer.toString();
			if ( TCLUtils.isCompleteStatement(test) ) {
				buffer = new StringBuilder();

				ThreadManager.instance().submitWork(new BranchEvaluationResult(new StringParameter(test), ctx, new EvaluationResult.EvalCallback() {

					public EvaluationResult result(EvaluationResult e) {
						if ( e.getCode() == Code.ERROR ) {
							arg0.getForWhom().sendRawMessage(ChatColor.RED + "Error: " + e.getValue().asString());
						}

						if ( e.getValue() != null ) ctx.put("ans", e.getValue());
						else ctx.put("ans", Parameter.from(""));

						return EvaluationResult.OK;
					}

				}));

			}
			return this;
		}

		@Override
		public boolean blocksForInput(ConversationContext arg0) {
			return true;
		}

		@Override
		public String getPromptText(ConversationContext arg0) {
			if ( buffer.length() > 0 ) return "--->";
			return "TCL>";
		}

	}

	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {

		if (!sender.isOp())
			return false;

		Queue<String> qargs = new LinkedList<String>(Arrays.asList(args));
		String action = label;

		final Context ctx = getContext(sender);

		if ( label.equals("scriptmode") ) {
			if (!(sender instanceof Player)) return false;
			final Player p = (Player) sender;

			p.beginConversation(new Conversation(plugin, p, new ScriptModePrompt(plugin, ctx)));
			return true;
		}

		if (label.equals("parchment") || label.equals("p")) {
			action = qargs.poll();
		}

		if (action == null) return false;



		if (action.equals("cast") || action.equals("c")) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE.toString() + "Warning: /cast is deprecated, and will likely change function.  Use /tcl instead!");
			action = "tcl";
		}

		if (action.equals("tcl") || action.equals("t")) {
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
			ThreadManager.instance().submitWork(new BranchEvaluationResult(new StringParameter(b.toString()), ctx, new EvaluationResult.EvalCallback() {

				public EvaluationResult result(EvaluationResult e) {
					if ( e.getCode() == Code.ERROR ) {
						sender.sendMessage(ChatColor.RED +"Error: " + e.getValue().asString());
					}

					return EvaluationResult.OK;
				}

			}));




		} else if (action.equals("run")) {
			String file = qargs.poll() + ".tcl";
			File folder = FSUtils.findOrCreateDirectory(plugin.getDataFolder(), "runnable");
			File rfile = FSUtils.findFile(folder, file);
			if (rfile == null) {
				sender.sendMessage("Unknown file " + file);
				return true;
			}
			Reader reader;
			try {
				reader = new InputStreamReader(new FileInputStream(rfile));
				ArrayList<Parameter> argz = new ArrayList<Parameter>();
				while ( qargs.size() > 0 ) argz.add(Parameter.from(qargs.poll()));
				ctx.put("args", ListParameter.from(argz));
				TCLUtils.evaluate(reader, ctx);
			} catch (FileNotFoundException e) {
				sender.sendMessage("Unknown file 2 " + file);
			}

		} else if ( action.equals("tclhelp") || action.equals("help") ) {
			if ( qargs.size() < 1 ) {
				sender.sendMessage("Please specify a command you would like help with:");
				StringBuilder b = new StringBuilder();
				for ( TCLCommand c : this.plugin.getSpellFactory().getAll().values() ) {
					if ( b.length() > 0 ) b.append(" ");
					b.append(c.getName());
					if ( b.length() > 100 ) {
						sender.sendMessage(b.toString());
						b.setLength(0);
					}
				}
			}
			String helpcmd = qargs.poll();
			TCLCommand c = this.plugin.getSpellFactory().get(helpcmd);
			if ( c == null ) {
				sender.sendMessage("No command found by that name.");
			}

			sender.sendMessage(WikiToMinecraft(c.getHelpText()));

		} else {
			sender.sendMessage("Unknown action " + action);
		}

		return true;
	}

	private Context getContext(CommandSender sender) {
		final Context ctx = commandctx.createSubContext();
		ctx.setSpellFactory(plugin.getSpellFactory());
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ctx.setCaster(Parameter.from(p));
			ctx.setWorld(Parameter.from(p.getWorld()));
			ctx.setSource("command");
		} else {
			ctx.setCaster(Parameter.from("CONSOLE"));
		}
		return ctx;
	}

	private static String WikiToMinecraft(String str) {
		str = str.replaceAll("=== (.*?) ===", "" + ChatColor.BOLD + ChatColor.LIGHT_PURPLE + "========== $1 ==========" + ChatColor.RESET);
		str = str.replaceAll("\\[\\[(.*?)\\]\\]", "");
		str = str.replaceAll("\\*\\*(.*?)\\*\\*", ChatColor.BOLD + "$1" + ChatColor.RESET);
		str = str.replaceAll("//(.*?)//", ChatColor.ITALIC + "$1" + ChatColor.RESET );
		str = str.replaceAll("\n(\\s+)\\\\\\\\(.*?)","\n  $2");
		str = str.replaceAll("\n+","\n");

		return str;

	}

}
