package com.basicer.parchment.bukkit;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 * Created by basicer on 2/8/14.
 */
public class GlobalListenerHeavy implements Listener {
	private  ParchmentPlugin plugin;

	public GlobalListenerHeavy(ParchmentPlugin instance) {
		plugin = instance;
	}



	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event) {
		plugin.handleEvent(event);
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    	/*
        if (event.getPlayer().getItemInHand().getType() == Material.STRING) {
            if (event.getRightClicked() instanceof Wolf) {
                Wolf wolf = (Wolf) event.getRightClicked();
                plugin.debugMessage("Is tamed: " + wolf.isTamed());
                plugin.debugMessage("Owner: " + wolf.getOwner());
                wolf.setTamed(wolf.isTamed() ? false : true);
                plugin.debugMessage("Is tamed: " + wolf.isTamed());
                plugin.debugMessage("Owner: " + wolf.getOwner());
            }
            event.getPlayer().setItemInHand(null);
        }
		*/
		plugin.handleEvent(event);
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
    	/*
        if (event.getPlayer().getItemInHand().getType() == Material.SLIME_BALL && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            plugin.debugMessage("Block name: " + event.getClickedBlock().getType());
            plugin.debugMessage("Block location: " + event.getClickedBlock().getX() + ", " + event.getClickedBlock().getY() + ", " + event.getClickedBlock().getZ());
            plugin.debugMessage("Block data: " + event.getClickedBlock().getData());
            plugin.debugMessage("Block LightLevel: " + event.getClickedBlock().getLightLevel());
            plugin.debugMessage("Block Chunk: " + event.getClickedBlock().getChunk().toString());
        }
        */

		plugin.handleEvent(event);
	}


	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		plugin.handleEvent(event);
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		plugin.handleEvent(event);
	}


	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		plugin.handleEvent(event);
	}


	@EventHandler
	public void onChunkPopulate(ChunkPopulateEvent event) {
		plugin.handleEvent(event);
	}


	@EventHandler
	public void onVehicleMove(VehicleMoveEvent event) {
		plugin.handleEvent(event);
	}

}
