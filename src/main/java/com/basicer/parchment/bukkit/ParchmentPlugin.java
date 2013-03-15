package com.basicer.parchment.bukkit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import com.basicer.parchment.Debug;
import com.basicer.parchment.ScriptedSpell;
import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.FizzleException;
import com.basicer.parchment.Context;
import com.basicer.parchment.SpellFactory;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;

import com.basicer.parchment.craftbukkit.Book;
import com.basicer.parchment.parameters.*;
import com.basicer.parchment.spells.Heal;
import com.basicer.parchment.spells.Spout;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.craftbukkit.libs.jline.internal.Log.Level;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
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
import org.yaml.snakeyaml.reader.StreamReader;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import net.minecraft.server.*;

public class ParchmentPlugin extends JavaPlugin implements Listener, PluginMessageListener {

	ProtocolManager	manager;
	SpellFactory	spellfactory;
	BukkitRunnable  loader;
	
	public void onDisable() {
		loader.cancel();
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		getLogger().info("Framework Disabled");

	}

	
	public void onEnable() {
		this.saveDefaultConfig();
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		getLogger().info("Framework Enabled");
		spellfactory = new SpellFactory();
		
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
		
		final Logger logger = this.getLogger();
		loader = new BukkitRunnable() {
			long wrote = 0;
			public void run() {
				File scripts = FSUtils.findOrCreateDirectory(base, "spells");
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

	}

	private Context createContext(Player p) {
		Context ctx = new Context();
		ctx.setSpellFactory(spellfactory);
		ctx.setCaster(Parameter.from(p));
		ctx.setWorld(Parameter.from(p.getWorld()));
		ctx.setServer(Parameter.from(p.getServer()));
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
		Book.ensureSpellWritten(e.getEntity().getItemStack());
	}


	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack holding = p.getItemInHand();
		if ( holding != null ) Book.ensureSpellWritten(holding);
		TCLCommand s = null;
		Context ctx = createContext(p);
		
		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (holding.getType() == org.bukkit.Material.BOOK_AND_QUILL || holding.getType() == org.bukkit.Material.WRITTEN_BOOK) {
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

				s = new ScriptedSpell("SomeBook", new PushbackReader(new StringReader(sb.toString())), spellfactory);
			

				ctx.setSource("wand");
	
				Parameter[] ws = new Parameter[1];
				ws[0] = Parameter.from("want");
				Context ctx2 = s.bindContext(ws, ctx);
				s.extendedExecute(ctx2, null);
				e.setCancelled(true);
				return;
			}
		}
		
		// p.sendMessage("MATERIAL IS " + holding.getType().toString());
		
		String ss = Book.readSpell(holding);
		if ( ss == null ) {
			p.sendMessage("Your blade is dull");
			e.setCancelled(false);
			return;
		}
		this.getLogger();
		p.sendMessage("Your blade is: " + ss + "/" + ss.length());
		
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
		scmd.executeBinding(binding, ctx, null);
		e.setCancelled(ctx.get("cancel").asBoolean());
		
		
		

	}

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {

	}

	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		player.sendMessage("I hear you like books");

	}

}
