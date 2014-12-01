package com.basicer.parchment.spells;

import com.basicer.parchment.Context;
import com.basicer.parchment.TargetedCommand;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.Parameter;
import org.bukkit.entity.Player;

/*
import pgDev.bukkit.DisguiseCraft.DisguiseCraft;
import pgDev.bukkit.DisguiseCraft.api.DisguiseCraftAPI;
import pgDev.bukkit.DisguiseCraft.disguise.DisguiseType;
*/

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 7/7/13
 * Time: 11:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Disguise extends TargetedCommand {

	@Override
	public boolean supportedByServer() {
			//try { return DisguiseCraft.getAPI() != null; }
			//catch ( NoClassDefFoundError ex ) {
				return false;
			//}
	}

	@Override
	public FirstParameterTargetType getFirstParameterTargetType(Context ctx) {
		return FirstParameterTargetType.FuzzyMatch;
	}

	@Override
	public String[] getArguments() { return new String[] {"-remove", "nue?", "nueData?"}; }

	public Parameter affect(PlayerParameter target, Context ctx) {
		Player player = target.asPlayer(ctx);
		Parameter nue = ctx.get("nue");
		if ( !supportedByServer() ) fizzle("Disguise ot supported by server!");
		return null;  //Remove when we want to reenable this.
		/*
		DisguiseCraftAPI api = DisguiseCraft.getAPI();

		if ( ctx.get("remove") != null ) {
			api.undisguisePlayer(player);
		}

		if ( nue != null ) {

			pgDev.bukkit.DisguiseCraft.disguise.Disguise disguise = null;

			if ( nue instanceof  PlayerParameter ) {
				disguise = new pgDev.bukkit.DisguiseCraft.disguise.Disguise(api.newEntityID(), DisguiseType.Player);
				disguise.setSingleData(((PlayerParameter) nue).asPlayer(ctx).getPlayerListName());
			} else {

				DisguiseType type = nue.asEnum(DisguiseType.class);

				if ( type != null ) {
					disguise = new pgDev.bukkit.DisguiseCraft.disguise.Disguise(api.newEntityID(), type);
				} else {
					fizzle("No such disguise type: " + nue.asString(ctx));
				}

				Parameter nueData = ctx.get("nueData");
				if  ( nueData != null ) disguise.setSingleData(nueData.asString(ctx));
			}

			if ( api.isDisguised(player) ) {
				api.changePlayerDisguise(player, disguise);
			} else {
				api.disguisePlayer(player, disguise);
			}
		}

		if ( !api.isDisguised(player) ) return  Parameter.EmptyString;
		pgDev.bukkit.DisguiseCraft.disguise.Disguise d = api.getDisguise(player);

		return Parameter.from(disguiseToString(d));
		*/
	}

	/*
	private String disguiseToString(pgDev.bukkit.DisguiseCraft.disguise.Disguise d) {
		return d.type.toString();
	}
	*/
}
