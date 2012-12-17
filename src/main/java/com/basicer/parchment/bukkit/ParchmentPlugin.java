package com.basicer.parchment.bukkit;

import com.basicer.parchment.Spell;
import com.basicer.parchment.SpellContext;
import com.basicer.parchment.craftbukkit.Book;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.spells.Heal;
import com.basicer.parchment.spells.SpellFactory;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
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
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

import net.minecraft.server.*;

public class ParchmentPlugin extends JavaPlugin implements Listener {

	ProtocolManager	manager;

	public void onDisable() {
		getLogger().info("Framework Disabled");
	}

	public void onEnable() {
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(this, this);
		getLogger().info("Framework Enabled");

		if (pm.getPlugin("ProtocolLib") != null) {
			manager = ProtocolLibrary.getProtocolManager();
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!sender.isOp())
			return false;
		SpellContext ctx = new SpellContext();
		if (sender instanceof Player) {
			ctx.setCaster(Parameter.from((Player) sender));
		} else {
			return false;
		}
		
		Spell s = SpellFactory.get(args[0]);
		s.cast(ctx);
		
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
			p.sendMessage("MATERIAL IS " + holding.getType().toString());
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
				SpellContext ctx = new SpellContext();
				ctx.setTarget(Parameter.from(p));
				ctx.setCaster(Parameter.from(p));
				Heal h = new Heal();
				h.cast(ctx);
			} else {
				p.sendMessage("Couldnt do " + action);
			}
			// e.getClickedBlock().breakNaturally(e.getPlayer().getItemInHand());
		} 


	}

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		ItemStack is = p.getInventory().getItem(e.getNewSlot());

		CraftItemStack cis = (CraftItemStack) is;
		if (is == null)
			return;
		net.minecraft.server.ItemStack ms = cis.getHandle();
		if (ms == null) {
			p.sendMessage("No handle");
			;
			return;
		}
		NBTTagCompound display = ms.getTag();
		if (display == null)
			return;
		// display = display.getCompound("display");
		// if ( display == null ) return;
		// String name = display.getString("Name");
		NBTTagList lst = display.getList("pages");
		if (lst == null)
			return;
		String name = ((NBTTagString) lst.get(0)).toString();
		if (name == null)
			return;
		p.sendMessage("Name is" + name);

	}

}
