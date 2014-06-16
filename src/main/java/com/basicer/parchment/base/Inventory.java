package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.OperationalTargetedCommand;
import com.basicer.parchment.parameters.*;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Hopper;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by basicer on 4/27/14.
 */
public class Inventory extends OperationalTargetedCommand<OpaqueParameter<org.bukkit.inventory.Inventory>> {
	public static Class<? extends OperationalTargetedCommand<?>> getBaseClass() { return LEntity.class; }

	@Override
	public String[] getAliases() { return new String[] {"inv"}; }

	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
	}



	public Parameter affect(PlayerParameter target, Context ctx) {
		Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());

		return OperationalTCLCommand.operationalDispatch(
				this,
				org.bukkit.inventory.Inventory.class,
				OpaqueParameter.create(target.asPlayer(ctx).getInventory()),
				ctx,
				args
		);
	}

	public org.bukkit.inventory.Inventory castToInventory(Parameter ptr, Context ctx) {
		if ( ptr instanceof OpaqueParameter ) {
			return ((OpaqueParameter<org.bukkit.inventory.Inventory>)ptr).getValue();
		} else if ( ptr instanceof PlayerParameter ) {
			return ((PlayerParameter) ptr).asPlayer(ctx).getInventory();
		} else if ( ptr instanceof EntityParameter ) {
			Entity lent = ((LivingEntityParameter)ptr).asEntity(ctx);
			if ( lent instanceof InventoryHolder ) {
				return ((InventoryHolder)lent).getInventory();
			}
		} else if ( ptr instanceof BlockParameter ) {
			BlockState meta = ptr.as(org.bukkit.block.Block.class).getState();
			if ( meta instanceof InventoryHolder ) {
				return ((InventoryHolder)meta).getInventory();
			}
		}

		throw new FizzleException("Couldn't cast to inventory");
	}

	public Parameter affect(Parameter target, Context ctx) {
		Queue<Parameter> args = new LinkedList<Parameter>(ctx.getArgs());

		return OperationalTCLCommand.operationalDispatch(
				this,
				org.bukkit.inventory.Inventory.class,
				OpaqueParameter.create(castToInventory(target, ctx)),
				ctx,
				args
		);
	}

	public void copyInventory(org.bukkit.inventory.Inventory from, org.bukkit.inventory.Inventory to) {
		for ( int i = 0; i < from.getSize(); ++i ) {
			to.setItem(i, from.getItem(i));
		}
	}

	public Parameter copyToOperation(org.bukkit.inventory.Inventory inv, Context ctx, Parameter target) {
		copyInventory(inv, castToInventory(target, ctx));
		return OpaqueParameter.create(inv);
	}


	public Parameter setFromOperation(org.bukkit.inventory.Inventory inv, Context ctx, Parameter target) {
		copyInventory(castToInventory(target, ctx), inv);
		return OpaqueParameter.create(inv);
	}

	public Parameter debugOperation(org.bukkit.inventory.Inventory inv, Context ctx) {

		for ( ItemStack i : inv.getContents() ) {
			if ( i != null ) ctx.sendDebugMessage(i.toString());
		}

		return Parameter.from("taco");
	}


}
