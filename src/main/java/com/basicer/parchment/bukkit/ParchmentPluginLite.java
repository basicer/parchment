package com.basicer.parchment.bukkit;

import com.basicer.parchment.Debug;
import com.basicer.parchment.SpellFactory;
import com.basicer.parchment.ThreadManager;
import com.basicer.parchment.parameters.*;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.mcstats.Metrics;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 9/29/13
 * Time: 11:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParchmentPluginLite extends JavaPlugin {
	protected ProtocolManager 	manager;
	protected SpellFactory		spellfactory;
	protected BukkitRunnable 	loader;
	protected Metrics			metrics;


	protected Permission 		vault_permission;
	protected Economy			vault_economy;
	protected Chat				vault_chat;

	private class TCLStepper extends BukkitRunnable {
		public void run() {
			long next = ThreadManager.instance().doWork();
			new TCLStepper().runTaskLater(getThis(), 1); //Check every tick, most of the time we dont do anything.
		}
	}

	//Lol Java.
	private ParchmentPluginLite getThis() { return this; }

	public static ParchmentPluginLite instance() { return (ParchmentPluginLite)Bukkit.getServer().getPluginManager().getPlugin("Parchment"); }

	public void onEnable() {
		this.saveDefaultConfig();
		spellfactory = new SpellFactory();


		try {
			metrics = new Metrics(this);
			metrics.createGraph("Scripts").addPlotter(new Metrics.Plotter("Scripts") {
				@Override
				public int getValue() {
					Debug.trace("You have %d Scripts", spellfactory.getScriptCommandCount());
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
		Parameter.RegisterParamaterType(VectorParameter.class);
		Parameter.RegisterParamaterType(MaterialParameter.class);
		Parameter.RegisterParamaterType(ServerParameter.class);
		Parameter.RegisterParamaterType(WorldParameter.class);
		Parameter.RegisterParamaterType(ItemParameter.class);

		PluginManager pm = this.getServer().getPluginManager();
		if (pm.getPlugin("ProtocolLib") != null) {
			manager = ProtocolLibrary.getProtocolManager();
		}

		if ( pm.getPlugin("Vault") != null ) {
			RegisteredServiceProvider<Permission> perm = getServer().getServicesManager().getRegistration(Permission.class);
			if ( perm != null ) {
				vault_permission = perm.getProvider();
			}
			RegisteredServiceProvider<Economy> eco = getServer().getServicesManager().getRegistration(Economy.class);
			if ( eco != null ) {
				vault_economy = eco.getProvider();
			}
			RegisteredServiceProvider<Chat> chat = getServer().getServicesManager().getRegistration(Chat.class);
			if ( chat != null ) {
				vault_chat = chat.getProvider();
			}

		} else {
			getLogger().info("No Vault");
		}

		spellfactory.load();

		CommandExecutor x = new ParchmentCommandExecutor(this);
		getCommand("cast").setExecutor(x);
		getCommand("tcl").setExecutor(x);
		getCommand("parchment").setExecutor(x);
		getCommand("scriptmode").setExecutor(x);

		new TCLStepper().runTaskLater(this, 0);
	}

	public SpellFactory getSpellFactory() {
		return spellfactory;
	}

	public Permission getVaultPermission() {
		return vault_permission;
	}

	public Economy getVaultEconomy() {
		return vault_economy;
	}

	public Chat getVaultChat() {
		return vault_chat;
	}
}
