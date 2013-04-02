package com.basicer.parchment.bukkit;

import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.painting.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.*;
import org.bukkit.event.weather.*;
import org.bukkit.event.world.*;


public class GlobalListener implements Listener {
    private final ParchmentPlugin plugin;

    public GlobalListener(ParchmentPlugin instance) {
        plugin = instance;
    }
    
    /* Block */
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockCanBuild(BlockCanBuildEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockForm(BlockFormEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
    	plugin.handleEvent(event);;
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
    	plugin.handleEvent(event);
    }

    //@EventHandler
    //public void onBlockPhysics(BlockPhysicsEvent event) {
    //	plugin.handleEvent(event);
    //}

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    	plugin.handleEvent(event);
    }

    //@EventHandler
    //public void onBlockRedstoneChange(BlockRedstoneEvent event) {
    //	plugin.handleEvent(event);
    //}

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
    	plugin.handleEvent(event);
    }
    
    /* Enity */
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onCreeperPower(CreeperPowerEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityCombust(EntityCombustEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityDamageByBlock(EntityDamageByBlockEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
    	plugin.handleEvent(event);
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
    	/*
        Entity entity = event.getEntity();
        if (entity instanceof Squid) {
            List<ItemStack> drops = event.getDrops();
            int count = drops.get(0).getAmount();
            drops.clear();
            drops.add(new ItemStack(Material.APPLE, count));
        }
		*/

        plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityInteract(EntityInteractEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onExplosionPrime(ExplosionPrimeEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
   public void onFoodLevelChange(FoodLevelChangeEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPigZap(PigZapEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPaintingBreak(PaintingBreakEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPaintingPlace(PaintingPlaceEvent event) {
    	plugin.handleEvent(event);
    }
    
    /* Inventory */
    
    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
    	plugin.handleEvent(event);
    }
    
    /* Player */
    
    @EventHandler
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerEggThrow(PlayerEggThrowEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        /*if (!event.getState().equals(State.FISHING)) {
            event.setCancelled(true);
        }*/
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
    public void onItemHeldChange(PlayerItemHeldEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerPreLogin(PlayerPreLoginEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
    	plugin.handleEvent(event);
    } 
    
    /* Vehicle */
    
    public void onVehicleBlockCollision(VehicleBlockCollisionEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
    	plugin.handleEvent(event);
    }

    //@EventHandler
    //public void onVehicleUpdate(VehicleUpdateEvent event) {
    //    plugin.handleEvent(event);
    //}
    
    /* Weather */
    
    @EventHandler
    public void onLightningStrike(LightningStrikeEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onThunderChange(ThunderChangeEvent event) {
        plugin.handleEvent(event);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
    	plugin.handleEvent(event);
    }
 
    /* World */
    
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onChunkPopulate(ChunkPopulateEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onPortalCreate(PortalCreateEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onSpawnChange(SpawnChangeEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onWorldInit(WorldInitEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onWorldSave(WorldSaveEvent event) {
    	plugin.handleEvent(event);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
    	plugin.handleEvent(event);
    }
}
