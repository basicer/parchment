package com.basicer.parchment.extra;

import com.basicer.parchment.*;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.parameters.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;


/**
 * Created by basicer on 1/20/14.
 */
public class IMenu extends TCLCommand {

	@Override
	public String getName() { return "imenu"; }

	@Override
	public String[] getArguments() { return new String[] { "-empty=", "-size=", "-timeout=", "-title=", "-list", "player", "args" }; }

	public class MenuListener implements Listener {
		private org.bukkit.entity.Player player;
		public Integer value = null;
		public MenuListener(org.bukkit.entity.Player p ) {
			this.player = p;
		}

		@EventHandler
		public void onClose(InventoryCloseEvent e) {
			if ( !e.getPlayer().equals(player) ) return;
			if ( value == null ) value = -1;
			HandlerList.unregisterAll(this);
		}

		@EventHandler
		public void onClick(InventoryClickEvent e) {
			if ( !e.getWhoClicked().equals(player) ) return;

			e.setCancelled(true);
			value = e.getSlot();
			player.closeInventory();
			HandlerList.unregisterAll(this);
		}
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		PlayerParameter pp = ctx.get("player").cast(PlayerParameter.class);

		final Player p = pp.asPlayer(ctx);
		final MenuListener listener = new MenuListener(p);


		int size = 36;
		if ( ctx.has("size") ) size = ctx.get("size").asInteger(ctx);

		Inventory inv;
		if ( ctx.has("title") ) {
			inv = Bukkit.createInventory(p, size, ctx.get("title").asString(ctx));
		} else {
			inv = Bukkit.createInventory(p, size);
		}

		final ItemStack[] contents = new ItemStack[inv.getSize()];


		if ( ctx.has("empty") ) {
			ItemStack empty = ctx.get("empty").as(ItemStack.class);
			for ( int i = 0; i < contents.length; ++i ) {
				contents[i] = empty;
			}
		}


		Iterator<Parameter> lst = ctx.has("list") ? ctx.getArgs().get(0).iterator() : ctx.getArgs().iterator();

		int i = 0;
		while ( lst.hasNext() ) {
			contents[i++] = lst.next().as(ItemStack.class, ctx);
		}

		final int timeout = ctx.has("timeout") ? (int)(ctx.get("timeout").asDouble(ctx) * 1000) : 0;
		final long start = System.currentTimeMillis();

		inv.setContents(contents);
		p.openInventory(inv);
		Bukkit.getServer().getPluginManager().registerEvents(listener, ParchmentPluginLite.instance() );

		BranchEvaluationResult result = new BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
			EvaluationResult.EvalCallback rerun = this;
			if ( timeout > 0 && (System.currentTimeMillis() - start) > timeout ) {
				p.closeInventory();
				return EvaluationResult.makeError(Parameter.from("timeout"));
			}
			if ( listener.value == null ) return new BranchEvaluationResult(null, null, rerun, 100);
				if ( listener.value == -1 ) return EvaluationResult.makeError("cloased");
				return EvaluationResult.makeOkay(ItemParameter.from(contents[listener.value.intValue()]));

			}
		});

		return result;
	}
}
