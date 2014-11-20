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

import com.basicer.parchment.base.Server;
import com.basicer.parchment.parameters.*;

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


public class ParchmentPlugin extends ParchmentPluginLite implements PluginMessageListener {

	protected GlobalListener	listener;
	protected GlobalListenerHeavy	listenerHeavy;

	public static ParchmentPlugin getInstance() {
		return (ParchmentPlugin)Bukkit.getPluginManager().getPlugin("Parchment");
	}

	public void onDisable() {
		loader.cancel();
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		super.onDisable();
	}


	public void onEnable() {
		super.onEnable();
		listener = new GlobalListener(this);
		//listenerHeavy = new GlobalListenerHeavy(this);

		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new ParchmentEventListener(this), this);


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
						spellfactory.addCustomSpell(sname, new ScriptedSpell(sname, new InputStreamReader(fis), spellfactory));
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
		if ( listenerHeavy != null ) pm.registerEvents(listenerHeavy, this);

        File autoexec = FSUtils.findOrCreateDirectory(base, "autoexec");
        for (File s : autoexec.listFiles()) {
            if (s.isDirectory())
                continue;
            if (!s.canRead())
                continue;
            if (!s.getName().endsWith(".tcl"))
                continue;

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(s);
				TCLEngine engine = new TCLEngine(new InputStreamReader(fis), createContext(null));
                while ( engine.step() ) {}
                logger.info("Ran " + s.getAbsolutePath());
            } catch (FileNotFoundException e) {
                logger.warning("Couldnt load " + s.getAbsolutePath());

            } finally {
                try {
                    if ( fis != null ) fis.close();
                } catch (IOException e) {
                    //TODO: Im not even sure what to do about his one.
                }
            }

        }
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

	Context createContext(Player p) {
		Context ctx = new Context();
		ctx.setSpellFactory(spellfactory);
		if ( p != null ) {
			ctx.setCaster(Parameter.from(p));
			ctx.setWorld(Parameter.from(p.getWorld()));
		}
		ctx.put("origin", Parameter.from("createContext"));
		return ctx;
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



	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		player.sendMessage("I hear you like books");

	}

}
