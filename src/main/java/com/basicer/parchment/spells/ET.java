package com.basicer.parchment.spells;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.base.World;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.LocationParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class ET extends OperationalTCLCommand { 
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}
	
	public static Parameter spawnOperation(Parameter dummy, Context ctx, StringParameter stype, IntegerParameter id, LocationParameter where) {
		if ( id == null ) throw new FizzleException("Need id");
		if ( where == null ) throw new FizzleException("Need location");
		
		 PacketContainer newPacket = new PacketContainer(24);
		 	EntityType type = stype.asEnum(EntityType.class);
		 	if ( type == null ) throw new FizzleException("So, type isnt good.");
	        newPacket.getIntegers().
	            write(0, id.asInteger(ctx)).
	            write(1, (int) type.getTypeId()).
	            write(2, (int) (where.asLocation(ctx).getX() * 32)).
	            write(3, (int) (where.asLocation(ctx).getY() * 32)).
	            write(4, (int) (where.asLocation(ctx).getZ() * 32));
	        
	        //newPacket.getDataWatcherModifier().write(0, ghastWatcher);
	        
	        Entity entity = where.asLocation(ctx).getWorld().spawnEntity(where.asLocation(ctx), type);
	        WrappedDataWatcher watcher = WrappedDataWatcher.getEntityWatcher(entity).deepClone();
	        
	        
	        newPacket.getDataWatcherModifier().write(0,  watcher);
	        entity.remove();
	        try {
	        	for ( Player p : where.asWorld(ctx).getPlayers() )
	        		ProtocolLibrary.getProtocolManager().sendServerPacket(p, newPacket);
	        } catch (InvocationTargetException e) {
	            throw new FizzleException(e.getMessage());
	        }
	        
	        return Parameter.EmptyString;
	}
	
	public static Parameter spawnVOperation(Parameter dummy, Context ctx, IntegerParameter id, LocationParameter where) {
		if ( id == null ) throw new FizzleException("Need id");
		if ( where == null ) throw new FizzleException("Need location");
		
		 PacketContainer newPacket = new PacketContainer(0x17);

	        newPacket.getIntegers().
	            write(0, id.asInteger(ctx).intValue()). //a
	            write(1, (int) (where.asLocation(ctx).getX() * 32)).
	            write(2, (int) (where.asLocation(ctx).getY() * 32)).
	            write(3, (int) (where.asLocation(ctx).getZ() * 32)). //d
	            write(4, 0). //e Vel
	            write(5, 0). //f Vel
	            write(6, 0). //g Vel
	            write(7, 0). //h Yaw
	            write(8, 0). //i Pitch
	            write(9, 10); //j --type
	            
	        
	        //newPacket.getDataWatcherModifier().write(0, ghastWatcher);
	        
	       
	        try {
	        	for ( Player p : where.asWorld(ctx).getPlayers() )
	        		ProtocolLibrary.getProtocolManager().sendServerPacket(p, newPacket);
	        } catch (InvocationTargetException e) {
	            throw new FizzleException(e.getMessage());
	        }
	        
	        return Parameter.EmptyString;
	}
	
	public static Parameter rotateOperation(Parameter dummy, Context ctx, IntegerParameter id, IntegerParameter hop, IntegerParameter yaw, IntegerParameter pitch) {
		 PacketContainer newPacket = new PacketContainer(0x21);
	        newPacket.getIntegers().
	            write(0, id.asInteger(ctx));
	        newPacket.getBytes().
	        	write(2, (byte) hop.asInteger().intValue()).
	            write(3, (byte) yaw.asInteger().intValue()).
	            write(4, (byte) pitch.asInteger().intValue());
	        
	        //newPacket.getDataWatcherModifier().write(0, ghastWatcher);
	        
	        
	        try {
	        	for ( Player p : ctx.getCaster().as(org.bukkit.World.class).getPlayers() )
	        		ProtocolLibrary.getProtocolManager().sendServerPacket(p, newPacket);
	        } catch (InvocationTargetException e) {
	            throw new FizzleException(e.getMessage());
	        }
			return Parameter.EmptyString;
	
	}
	
	public static Parameter wtfOperation(Parameter dummy, Context ctx, LocationParameter where) {
		org.bukkit.World w = where.asLocation(ctx).getWorld();
		Location l = where.asLocation(ctx);
		l.setPitch(100.0f);
		l.setYaw(45.0f);
		Minecart e = (Minecart)w.spawnEntity(l, EntityType.MINECART);
		e.setPassenger(w.spawnEntity(where.asLocation(ctx), EntityType.SLIME));
		return Parameter.EmptyString;
		
	}
		
	
	public static Parameter rotate1Operation(Parameter dummy, Context ctx, IntegerParameter id, IntegerParameter yaw) {
		 PacketContainer newPacket = new PacketContainer(0x23);
	        newPacket.getIntegers().
	            write(0, id.asInteger(ctx));
	        newPacket.getBytes().
	            write(0, (byte) yaw.asInteger().intValue());
	        
	        //newPacket.getDataWatcherModifier().write(0, ghastWatcher);
	        
	        
	        try {
	        	for ( Player p : ctx.getCaster().as(org.bukkit.World.class).getPlayers() )
	        		ProtocolLibrary.getProtocolManager().sendServerPacket(p, newPacket);
	        } catch (InvocationTargetException e) {
	            throw new FizzleException(e.getMessage());
	        }
			return Parameter.EmptyString;
	
	}
		
	

}
