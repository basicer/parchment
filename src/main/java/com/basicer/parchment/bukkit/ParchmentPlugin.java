package com.basicer.parchment.bukkit;

import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.logging.Logger;

import com.basicer.parchment.*;

import com.basicer.parchment.parameters.*;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Plotter;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;


public class ParchmentPlugin extends ParchmentPluginLite implements Listener, PluginMessageListener {

	protected GlobalListener	listener;

	public static ParchmentPlugin getInstance() {
		return (ParchmentPlugin)Bukkit.getPluginManager().getPlugin("Parchment");
	}

	public void onDisable() {
		loader.cancel();
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		
	}


	public void onEnable() {
		super.onEnable();
		listener = new GlobalListener(this);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);


		Bukkit.getMessenger().registerIncomingPluginChannel(this, "MC|BEdit", this);

		final File base = this.getDataFolder();
		
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
					FileInputStream fis = null;
					try {
						 fis = new FileInputStream(s);
						PushbackReader reader = new PushbackReader(new InputStreamReader(fis));
						spellfactory.addCustomSpell(sname, new ScriptedSpell(sname, reader, spellfactory));
						logger.info("Loaded " + sname + " / " + time);
					} catch (FileNotFoundException e) {
						logger.warning("Couldnt load " + sname);

					} finally {
						try {
							if ( fis != null ) fis.close();
						} catch (IOException e) {
							//TODO: Im not even sure what to do about his one.
						}
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
		}
		ctx.put("origin", Parameter.from("createContext"));
		return ctx;
	}
	


	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent e) {
		//Book.ensureSpellWritten(e.getEntity().getItemStack());
	}


	@EventHandler
	public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent e) {
		Enumeration<TCLCommand> enu = this.spellfactory.getAll().elements();

		String[] parts = e.getMessage().split(" ");
		final String binding = parts[0].substring(1);


		Debug.info("Got command: " + binding);
		while ( enu.hasMoreElements() ) {
			TCLCommand cmd = enu.nextElement();
			if ( !(cmd instanceof ScriptedSpell )) continue;
			final ScriptedSpell s = (ScriptedSpell) cmd;
			if ( !s.canExecuteBinding("command:" + binding) ) continue;

			final Context ctx = createContext(null);
			ctx.setCaster(Parameter.from(e.getPlayer()));
			final ArrayList<Parameter> args = new ArrayList<Parameter>();
			for ( String part : parts ) args.add(Parameter.from(part));


			ThreadManager.instance().submitWork(new EvaluationResult.BranchEvaluationResult(null, ctx, new EvaluationResult.EvalCallback() {

				public EvaluationResult result(EvaluationResult e) {
					return s.executeBinding("command:" + binding, ctx, null, args);
				}

			}));



			e.setCancelled(true);

		}
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

		String binding = e.getEventName();
		if ( !binding.endsWith("Event") ) {
			return;
		}
		
		binding = binding.substring(0, binding.length() - 5).toLowerCase();
		//Bukkit.getLogger().info("->" + binding);
		if ( binding.equals("entityportalenter")) return;



		Enumeration<ScriptedSpell> enu = this.spellfactory.findAllWithBinding("bukkit:" + binding);
		while ( enu.hasMoreElements() ) {
			ScriptedSpell cmd = enu.nextElement();

			Debug.trace("Got h: " + cmd.getName());

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
			evt.writeIndex("event", Parameter.from(binding));
			
			ArrayList<Parameter> args = new ArrayList<Parameter>();
			args.add(evt);

			
			EvaluationResult er = cmd.executeBinding("bukkit:" + binding, ctx, null, args);
			TCLEngine ngn = new TCLEngine(er, ctx);   //TODO: We cant use this, can we use engine we have?
			while ( ngn.step() ) {}
			er = ngn.getEvaluationResult();
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
