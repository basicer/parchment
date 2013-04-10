package com.basicer.parchment.bukkit;

import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import com.basicer.parchment.Debug;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Context;
import com.basicer.parchment.SpellFactory;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.craftbukkit.Book;
import com.basicer.parchment.parameters.*;
import com.basicer.parchment.spells.Heal;
import com.basicer.parchment.spells.Spout;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.craftbukkit.libs.jline.internal.Log.Level;
import org.bukkit.event.Event;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;
import org.yaml.snakeyaml.reader.StreamReader;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import net.minecraft.server.*;

public class ParchmentPlugin extends JavaPlugin implements Listener, PluginMessageListener {

	ProtocolManager	manager;
	SpellFactory	spellfactory;
	BukkitRunnable  loader;
	Metrics			metrics;
	GlobalListener	listener;
	
	public void onDisable() {
		loader.cancel();
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		
	}

	
	public void onEnable() {
		this.saveDefaultConfig();
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		spellfactory = new SpellFactory();
		listener = new GlobalListener(this);
		
		try {
			metrics = new Metrics(this);
			metrics.createGraph("Scripts").addPlotter(new Plotter("Scripts") {
				@Override
				public int getValue() {
					Debug.info("You have %d Scripts", spellfactory.getScriptCommandCount());
					return spellfactory.getScriptCommandCount();
				}
				
			});
			metrics.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		
		Parameter.RegisterParamaterType(PlayerParameter.class);
		Parameter.RegisterParamaterType(LivingEntityParameter.class);
		Parameter.RegisterParamaterType(BlockParameter.class);
		Parameter.RegisterParamaterType(EntityParameter.class);
		
		Parameter.RegisterParamaterType(LocationParameter.class);
		Parameter.RegisterParamaterType(MaterialParameter.class);
		Parameter.RegisterParamaterType(ServerParameter.class);
		Parameter.RegisterParamaterType(WorldParameter.class);
		Parameter.RegisterParamaterType(ItemParameter.class);
		
		
		
		if (pm.getPlugin("ProtocolLib") != null) {
			manager = ProtocolLibrary.getProtocolManager();
		}
		
		CommandExecutor x = new ParchmentCommandExecutor(this);
		getCommand("cast").setExecutor(x);
		getCommand("parchment").setExecutor(x);
		getCommand("scriptmode").setExecutor(x);

		Bukkit.getMessenger().registerIncomingPluginChannel(this, "MC|BEdit", this);

		final File base = this.getDataFolder();
		
		spellfactory.load();
		
		
		if ( pm.getPlugin("Spout") != null ) {
			spellfactory.addBuiltinSpell(Spout.class);
		}
		
		writeWikiHelp();
		
		final Logger logger = this.getLogger();
		loader = new BukkitRunnable() {
			long wrote = 0;
			public void run() {
				File scripts = FSUtils.findOrCreateDirectory(base, "spells");
				Debug.trace("Scanning: " + scripts);
				if (scripts == null) return;
				long best = 0;
				for (File s : scripts.listFiles()) {
					if (s.isDirectory())
						continue;
					if (!s.canRead())
						continue;
					if (!s.getName().endsWith(".tcl"))
						continue;
					long time = s.lastModified();
					if ( time <= wrote ) continue;
					if ( best < s.lastModified() ) best = s.lastModified();
					
					String sname = s.getName().substring(0, (int) (s.getName().length() - 4));
					try {
						PushbackReader reader = new PushbackReader(new InputStreamReader(new FileInputStream(s)));
						spellfactory.addCustomSpell(sname, new ScriptedSpell(sname, reader, spellfactory));
						logger.info("Loaded " + sname + " / " + time);
					} catch (FileNotFoundException e) {
						logger.warning("Couldnt load " + sname);

					}

				}
				if ( best > wrote ) wrote = best;
			}
			
		};
		
		loader.run();
		loader.runTaskTimer(this, 100, 100);

		pm.registerEvents(listener, this);
	}

	private void writeWikiHelp() {
		StringBuilder b = new StringBuilder();
		List<TCLCommand> sx = new ArrayList<TCLCommand>();
		Enumeration<TCLCommand> en = spellfactory.getAll().elements();
		while ( en.hasMoreElements() ) sx.add(en.nextElement());
		
		
		Collections.sort(sx,  new Comparator<TCLCommand>() {
			public int compare(TCLCommand arg0, TCLCommand arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		
		for ( TCLCommand s : sx ) {
			b.append(s.getHelpText());
		}
		
		File help = new File(this.getDataFolder(), "help.txt");
		
		try {
			if ( !help.exists() ) help.createNewFile();
			FileWriter fw = new FileWriter(help);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(b.toString());
			out.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private Context createContext(Player p) {
		Context ctx = new Context();
		ctx.setSpellFactory(spellfactory);
		if ( p != null ) {
			ctx.setCaster(Parameter.from(p));
			ctx.setWorld(Parameter.from(p.getWorld()));
			ctx.setServer(Parameter.from(p.getServer()));
		} else {
			ctx.setServer(Parameter.from(Bukkit.getServer()));
		}
		ctx.put("origin", Parameter.from("createContext"));
		return ctx;
	}
	
	public SpellFactory getSpellFactory() {
		return spellfactory;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e) {
		//Book.ensureSpellWritten(e.getEntity().getItemStack());
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack holding = p.getItemInHand();
		//if ( holding != null ) Book.ensureSpellWritten(holding);
		TCLCommand s = null;
		Context ctx = createContext(p);
		
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if ( e.getPlayer().isOp() && (holding.getType() == org.bukkit.Material.BOOK_AND_QUILL || holding.getType() == org.bukkit.Material.WRITTEN_BOOK)) {
				BookMeta b = (BookMeta) holding.getItemMeta();

				if (e.getClickedBlock() != null && e.getClickedBlock().getType() == org.bukkit.Material.BOOKSHELF) {
					holding.setType(org.bukkit.Material.BOOK_AND_QUILL);
					e.setCancelled(true);
					return;
				}

				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < b.getPageCount(); ++i) {
					sb.append(b.getPage(i + 1));
					sb.append("\n");
				}
				// p.sendMessage(action);
				/*
				 * if ( action.startsWith("bridge") ) { for (Block bl :
				 * p.getLineOfSight(null, 40)) { if
				 * (bl.getLocation().distance(p.getLocation()) > 2)
				 * bl.getWorld()
				 * .getHighestBlockAt(bl.getLocation()).getRelative(0, 0,
				 * 0).setTypeId(66); } } else if ( action.startsWith("arrow") )
				 * { Arrow a = p.launchProjectile(Arrow.class);
				 * a.setVelocity(a.getVelocity().multiply(4)); } else if (
				 * action.startsWith("eval")) { p.sendMessage("Preformed " +
				 * action.substring(5)); p.performCommand(action.substring(5));
				 * } else if ( action.startsWith("heal") ) { Context ctx = new
				 * Context(); ctx.setTarget(Parameter.from(p));
				 * ctx.setCaster(Parameter.from(p)); Heal h = new Heal(); try {
				 * h.cast(ctx); } catch ( FizzleException fizzle ) {
				 * p.sendMessage("The spell fizzles"); } } else {
				 * p.sendMessage("Couldnt do " + action); }
				 */

				// e.getClickedBlock().breakNaturally(e.getPlayer().getItemInHand());

				ScriptedSpell ss = new ScriptedSpell("SomeBook", new PushbackReader(new StringReader(sb.toString())), spellfactory);
			

				ctx.setSource("wand");
				ss.executeBinding("cast", ctx, null);
				e.setCancelled(true);
				return;
			}
		}
		
		// p.sendMessage("MATERIAL IS " + holding.getType().toString());
		
		String ss = BindingUtils.getBinding(holding);
		if ( ss == null ) {
			Debug.trace("Your blade is dull");
			e.setCancelled(false);
			return;
		}
		this.getLogger();
		Debug.trace("Your blade is: " + ss + "/" + ss.length());
		
		TCLCommand cmd = spellfactory.get(ss);
		if ( cmd == null ) return;
		Spell scmd = (Spell) cmd;
		ctx.setSource("item");
		Parameter cancel = Parameter.from(e.isCancelled());
		Debug.trace(cancel.toString());
		ctx.put("cancel", cancel);
		
		String binding = "cast";
		switch ( e.getAction() ) {
			case LEFT_CLICK_AIR:
			case LEFT_CLICK_BLOCK:
				binding = "cast";
				break;
			case RIGHT_CLICK_AIR:
			case RIGHT_CLICK_BLOCK:
				binding = "alt";
				break;
			case PHYSICAL:
				return;
		}
		EvaluationResult r = scmd.executeBinding(binding, ctx, null);
		
		e.setCancelled(ctx.get("cancel").asBoolean());


	}


	public void handleEvent(Event e) {
		//Bukkit.getLogger().info(e.getEventName() + " : " + e.toString());
		Enumeration<TCLCommand> enu = this.spellfactory.getAll().elements();
		
		String binding = e.getEventName();
		if ( !binding.endsWith("Event") ) {
			return;
		}
		
		binding = binding.substring(0, binding.length() - 5).toLowerCase();
		//Bukkit.getLogger().info("->" + binding);
		 
		while ( enu.hasMoreElements() ) {
			TCLCommand cmd = enu.nextElement();
			if ( !(cmd instanceof ScriptedSpell )) continue;
			ScriptedSpell s = (ScriptedSpell) cmd;
			if ( !s.canExecuteBinding("bukkit:" + binding) ) continue;
			
			Context ctx = createContext(null);
			DictionaryParameter evt = new DictionaryParameter();
			if ( e instanceof Cancellable ) {
				Cancellable c = (Cancellable) e;
				ctx.put("cancel", Parameter.from(((Cancellable) e).isCancelled()));
			}
			
			Class<? extends Event> clazz = e.getClass();
			for ( Method m : clazz.getMethods() ) {
				String name = m.getName();
				if ( !name.startsWith("get") ) continue;
				name = name.substring(3).toLowerCase();
				if ( name.equals("handlers") ) continue;
				if ( name.equals("handlerlist") ) continue;
				try {
					Object o = m.invoke(e);
					Parameter from = Parameter.from(o);
					Debug.trace("Wrote %s as %s", name, from.asString());
					evt.writeIndex(name, from);
				} catch (IllegalAccessException e1) {
				} catch (IllegalArgumentException e1) {
				} catch (InvocationTargetException e1) {
				} catch (RuntimeException e1) {
					//e1.printStackTrace();
				}
			}
			evt.writeIndex("name", Parameter.from(binding));
			
			ArrayList<Parameter> args = new ArrayList<Parameter>();
			args.add(evt);

			
			EvaluationResult er = s.executeBinding("bukkit:" + binding, ctx, null, args);
			Debug.trace(" >>- " + er);
			
			
			for ( Method m : clazz.getMethods() ) {
				String name = m.getName();
				if ( !name.startsWith("set") ) continue;
				Debug.trace("Write ? %s", name);
				Class<?>[] types = m.getParameterTypes();
				if ( types.length != 1 ) continue;
				
				name = name.substring(3).toLowerCase();
				if ( !evt.hasIndex(name) ) continue;
				try {
					Debug.trace("Write Cast -> %s [%s] %s", name, types[0].getName(), evt.index(name).asString());
				
					Object nv = evt.index(name).as(types[0]);
					if ( nv == null ) continue;
					Debug.trace("Write -> %s %s", name, nv.toString());
					m.invoke(e, nv);
				} catch (IllegalAccessException e1) {
				} catch (IllegalArgumentException e1) {
				} catch (InvocationTargetException e1) {
				} catch (RuntimeException e1) {
					//e1.printStackTrace();
				}
			}
			
			
			if ( e instanceof Cancellable ) {
				Cancellable c = (Cancellable) e;
				c.setCancelled(ctx.get("cancel").asBoolean());
				Debug.trace(" >- " + ctx.get("cancel").asBoolean());
			}
		}
		
	}
	
	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {

	}

	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		player.sendMessage("I hear you like books");

	}

}
