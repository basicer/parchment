package com.basicer.parchment.bukkit;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.Parameter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * Created by basicer on 1/5/14.
 */
public class ParchmentEventListener implements Listener {
	private  ParchmentPlugin plugin;

	public ParchmentEventListener(ParchmentPlugin plugin) {
		this.plugin = plugin;
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
		Enumeration<TCLCommand> enu = plugin.getSpellFactory().getAll().elements();

		String[] parts = e.getMessage().split(" ");
		final String binding = parts[0].substring(1);


		Debug.info("Got command: " + binding);
		while ( enu.hasMoreElements() ) {
			TCLCommand cmd = enu.nextElement();
			if ( !(cmd instanceof ScriptedSpell)) continue;
			final ScriptedSpell s = (ScriptedSpell) cmd;
			if ( !s.canExecuteBinding("command:" + binding) ) continue;

			final Context ctx = plugin.createContext(null);
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
		Debug.info("Interacting!!!!");
		TCLCommand s = null;
		Context ctx = plugin.createContext(p);

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

				ScriptedSpell ss = new ScriptedSpell("SomeBook", new StringReader(sb.toString()), plugin.getSpellFactory());


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
		plugin.getLogger();
		Debug.trace("Your blade is: " + ss + "/" + ss.length());

		TCLCommand cmd = plugin.getSpellFactory().get(ss);
		if ( cmd == null ) return;
		TargetedCommand scmd = (TargetedCommand) cmd;
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
		Debug.info("Execute binding %s", binding);
		EvaluationResult r = scmd.executeBinding(binding, ctx, null);

		if ( r instanceof EvaluationResult.BranchEvaluationResult ) {
			TCLEngine ngn = new TCLEngine((EvaluationResult.BranchEvaluationResult)r);
			try {
				while (ngn.step() ) {}

			} catch ( Exception ex ) {
				System.out.println(ex.toString());
				for ( StackTraceElement ez : ex.getStackTrace() ) System.out.println(ez.toString());


			}

			r = ngn.getEvaluationResult();
		}

		boolean dcancel = ctx.get("cancel").asBoolean();
		e.setCancelled(dcancel);


	}

	@EventHandler
	public void onPlayerItemHeld(PlayerItemHeldEvent e) {

	}



}
