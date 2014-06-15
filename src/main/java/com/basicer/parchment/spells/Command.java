package com.basicer.parchment.spells;

import com.basicer.parchment.*;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.bukkit.ParchmentCommandExecutor;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 11/28/13
 * Time: 2:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class Command extends OperationalTCLCommand {
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}

	@Override
	public String[] getAliases() { return new String[] { "cmd" }; }

	private SimpleCommandMap getServerCommandMap(Server server) {
		try {
			return (SimpleCommandMap)server.getClass().getDeclaredMethod("getCommandMap").invoke(server);
		} catch (IllegalAccessException e) {
			throw new FizzleException("Bad Version of Bukkit");
		} catch (InvocationTargetException e) {
			throw new FizzleException("Bad Version of Bukkit");
		} catch (NoSuchMethodException e) {
			throw new FizzleException("Bad Version of Bukkit");
		}

	}

	private boolean removeCommandFromServer(Server server, String cmd) {
		SimpleCommandMap map = getServerCommandMap(Bukkit.getServer());
		Field known = null;
		try {
			known = SimpleCommandMap.class.getDeclaredField("knownCommands");
		} catch (NoSuchFieldException e) {
			throw new FizzleException("Bad Version of Bukkit");
		}
		known.setAccessible(true);
		Map<String, Command> knownCommands = null;
		try {
			knownCommands = (Map<String, Command>) known.get(map);
		} catch (IllegalAccessException e) {
			throw new FizzleException("Bad Version of Bukkit");
		}
		if ( !knownCommands.containsKey(cmd) ) return false;
		knownCommands.remove(cmd);
		return true;
	}

	public org.bukkit.command.Command getCommandFromServer(Server server, String cmd) {
		return (org.bukkit.command.Command)getServerCommandMap(Bukkit.getServer()).getCommand(cmd);
	}

	@Operation(aliases = {"del"})
	public Parameter removeOperation(Parameter dummy, Context ctx, StringParameter command) {
		String cmd = command.asString(ctx);
		org.bukkit.command.Command bcmd = getCommandFromServer(Bukkit.getServer(), cmd);
		if ( ! ( bcmd instanceof PluginCommand ) ) return Parameter.from(false);
		PluginCommand pcmd = (PluginCommand) bcmd;
		if ( pcmd.getPlugin() != ParchmentPluginLite.instance() ) Parameter.from(false);
		boolean result = removeCommandFromServer(Bukkit.getServer(), cmd);
		Debug.info("Remove %s from map = %s", cmd, result ? "true" : "false");
		return Parameter.from(result);
	}

	@Operation()
	public Parameter listOperation(Parameter dummy, Context ctx) {
		ArrayList<Parameter> result = new ArrayList<Parameter>();
		for ( org.bukkit.command.Command bcmd : getServerCommandMap(Bukkit.getServer()).getCommands() ) {
			if ( ! ( bcmd instanceof PluginCommand ) ) continue;
			PluginCommand pcmd = (PluginCommand) bcmd;
			if ( pcmd.getPlugin() != ParchmentPluginLite.instance() ) continue;
			if ( pcmd.getExecutor() instanceof ParchmentCommandExecutor ) continue;
			result.add(Parameter.from(pcmd.getName()));
		}
		return ListParameter.from(result);
	}

	@Operation()
	public Parameter addOperation(Parameter dummy, final Context ctx, StringParameter command, StringParameter body) {
		removeOperation(dummy, ctx, command);
		String cmd = command.asString(ctx);
		SimpleCommandMap map = getServerCommandMap(Bukkit.getServer());
		final StringParameter tcl_code = body;

		final Context ctxx_up = new Context();
		ctxx_up.setSpellFactory(ctx.getCommandFactory());
		CommandExecutor ex = new CommandExecutor() {
			public boolean onCommand(final CommandSender sender, org.bukkit.command.Command command, String s, String[] strings) {
				Debug.info("Running code + " + tcl_code);
				final Context ctxx = ctxx_up.createSubContext();
				ctxx.setCaster(Parameter.from(sender));

				ArrayList<Parameter> args = new ArrayList<Parameter>();
				for ( String arg : strings ) {
					args.add(Parameter.from(arg));
				}
				ctxx.put("args", ListParameter.from(args));
				ThreadManager.instance().submitWork(new BranchEvaluationResult(tcl_code, ctxx, new EvaluationResult.EvalCallback() {

					public EvaluationResult result(EvaluationResult e) {
						if ( e.getCode() == EvaluationResult.Code.ERROR ) {
							sender.sendMessage(ChatColor.RED +"Error: " + e.getValue().asString());
						}

						return EvaluationResult.OK;
					}

				}));

				return true;
			}
		};

		try {
			Constructor<?> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
			constructor.setAccessible(true);
			PluginCommand myCommand = (PluginCommand) constructor.newInstance(cmd, ParchmentPluginLite.instance());
			myCommand.setExecutor(ex);
			map.register(cmd, myCommand);
		} catch ( Exception ex2 ) {

		}

		return Parameter.from(true);
	}

}
