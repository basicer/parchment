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

import com.basicer.parchment.Spell;
import com.basicer.parchment.Spell.FizzleException;
import com.basicer.parchment.Context;
import com.basicer.parchment.SpellFactory;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLParser;
import com.basicer.parchment.craftbukkit.Book;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.spells.Heal;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.util.Vector;
import org.yaml.snakeyaml.reader.StreamReader;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import net.minecraft.server.*;

public class ParchmentPlugin extends JavaPlugin implements Listener, PluginMessageListener {

	ProtocolManager	manager;

	public void onDisable() {
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
		getLogger().info("Framework Disabled");
		
	}

	public void onEnable() {
		this.saveDefaultConfig();
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		getLogger().info("Framework Enabled");

		if (pm.getPlugin("ProtocolLib") != null) {
			manager = ProtocolLibrary.getProtocolManager();
		}
		
		Bukkit.getMessenger().registerIncomingPluginChannel(this, "MC|BEdit", this);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!sender.isOp())
			return false;
		
		Queue<String> qargs = new LinkedList<String>(Arrays.asList(args));
		String action = label; 
		
		if ( label.equals("parchment") || label.equals("p") ) {
			action = qargs.poll();
		}
		
		if ( action == null ) return false;
		Context ctx = new Context();
		if (sender instanceof Player) {
			Player p = (Player) sender;
			ctx.setCaster(Parameter.from(p));
			ctx.setWorld(Parameter.from(p.getWorld()));
			ctx.setServer(Parameter.from(p.getServer()));
		} else {
			return false;
		}
		
		
		
		if ( action.equals("cast") || action.equals("c") ) {
		
			
			StringBuilder b = null;
			while ( !qargs.isEmpty() ) {
				if ( b == null ) b = new StringBuilder();
				else b.append(" ");
				b.append(qargs.poll());
			}
			
			TCLParser.evaluate(b.toString(), ctx);
		} else if ( action.equals("run") ) {
			
			String file = qargs.poll() + ".tcl";
			File folder = this.getDataFolder();
			File rfile = null;
			for ( File f : folder.listFiles() ) {
				sender.sendMessage(f.getName());
				if ( f.isDirectory() ) continue;
				if ( !f.canRead() ) continue;
				if ( f.getName().equals(file) ) {
					rfile = f;
					break;
				}
			}
			if ( rfile == null ) {
				sender.sendMessage("Unknown file " + file);
				return true;
			}
			PushbackReader reader;
			try {
				reader = new PushbackReader(
						new InputStreamReader(new FileInputStream(rfile))
				);
				TCLParser.evaluate(reader, ctx);
			} catch (FileNotFoundException e) {
				sender.sendMessage("Unknown file 2 " + file);
			}
			
		} else {
			sender.sendMessage("Unknonw action " + action);
		}
		
		return true;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack holding = p.getItemInHand();
		if (holding.getType() != org.bukkit.Material.BOOK_AND_QUILL
				&& holding.getType() != org.bukkit.Material.WRITTEN_BOOK) {
			//p.sendMessage("MATERIAL IS " + holding.getType().toString());
			return;
		}
		p.sendMessage("Clicky click " + e.getAction().toString());
		Book b = Book.createFromBukkitItemStack(holding);

		if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (e.getClickedBlock() != null && e.getClickedBlock().getType() == org.bukkit.Material.BOOKSHELF) {
				b.unlock();
				e.setCancelled(true);
				return;
			}
			p.sendMessage(b.getFullText());
			String action = b.getFullText();
			/*
			if ( action.startsWith("bridge") ) {
				for (Block bl : p.getLineOfSight(null, 40)) {
					if (bl.getLocation().distance(p.getLocation()) > 2)
						bl.getWorld().getHighestBlockAt(bl.getLocation()).getRelative(0, 0, 0).setTypeId(66);
				}
			} else if ( action.startsWith("arrow") ) {
				Arrow a = p.launchProjectile(Arrow.class);
				a.setVelocity(a.getVelocity().multiply(4));
			} else if ( action.startsWith("eval")) {
				p.sendMessage("Preformed " + action.substring(5));
				p.performCommand(action.substring(5));
			} else if ( action.startsWith("heal") ) {
				Context ctx = new Context();
				ctx.setTarget(Parameter.from(p));
				ctx.setCaster(Parameter.from(p));
				Heal h = new Heal();
				try {
					h.cast(ctx);
				} catch ( FizzleException fizzle ) {
					p.sendMessage("The spell fizzles");
				}
			} else {
				p.sendMessage("Couldnt do " + action);
			}
			*/
			
			// e.getClickedBlock().breakNaturally(e.getPlayer().getItemInHand());
			Context ctx = new Context();
			ctx.setCaster(Parameter.from(p));
			ctx.setWorld(Parameter.from(p.getWorld()));
			ctx.setServer(Parameter.from(p.getServer()));
			TCLParser.evaluate(action, ctx);

		} 
		
	}

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {


	}

	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		player.sendMessage("I hear you like books");
		
	}

}
