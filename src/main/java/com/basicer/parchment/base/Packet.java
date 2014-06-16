package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.parameters.Parameter;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by basicer on 6/15/14.
 */
public class Packet extends TCLCommand {

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public String getHelpText() {
		return super.getHelpText();
	}

	@Override
	public String[] getArguments() { return new String[] { "players", "packet", "args" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		ProtocolManager manager = ParchmentPluginLite.instance().getProtocolManager();
		if ( manager == null ) return EvaluationResult.makeError("ProtocolLib not found");
		Parameter typep = ctx.get("packet");
		PacketType ptype;


		if (  typep.asInteger(ctx) != null ) {
			ptype = PacketType.findCurrent(PacketType.Protocol.PLAY, PacketType.Sender.SERVER, typep.asInteger(ctx) );
		} else {
			Collection<PacketType> found = PacketType.fromName(typep.asString());
			if ( found.size() > 1 ) return EvaluationResult.makeError("Ambiguous packet.");
			if ( found.size() < 1 ) return EvaluationResult.makeError("No packet type found.");
			ptype = found.iterator().next();
		}


		PacketContainer p = manager.createPacket(ptype);
		ArrayList<Parameter> args = ctx.getArgs();



		while ( !args.isEmpty() ) {
			String tg = args.remove(0).asString();
			char type = tg.charAt(0);
			int index = Integer.parseInt(tg.substring(1));
			if ( args.isEmpty() ) return EvaluationResult.makeError("Was expecting a value for field " + tg);
			Parameter value = args.remove(0);

			try {
				switch ( type ) {
					case 'i':
						p.getIntegers().write(index, value.asInteger(ctx));
						break;
					case 'd':
						p.getDoubles().write(index, value.asDouble(ctx));
						break;
					case 'f':
						p.getFloat().write(index, value.asDouble(ctx).floatValue());
						break;
					case 'a':
						p.getStrings().write(index, value.asString(ctx));
						break;
					case 's':
						p.getShorts().write(index, value.asInteger(ctx).shortValue());
						break;
					default:
						return EvaluationResult.makeError("Unknown type prefix " + type);

				}
			} catch ( Exception ex) {
				return EvaluationResult.makeError("Couldn't set " + type + ":" + index + " in " + ptype.name() + ": " + ex.getMessage());
			}
		}


		for ( Parameter player : ctx.get("players") ) {
			try {
				manager.sendServerPacket(player.as(org.bukkit.entity.Player.class), p);
			} catch ( InvocationTargetException ex ) {
				ctx.sendDebugMessage(ex.getMessage());
			}
		}

		return EvaluationResult.OK;
	}
}
