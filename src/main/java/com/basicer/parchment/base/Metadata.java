package com.basicer.parchment.base;

import com.basicer.parchment.*;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.parameters.*;
import org.bukkit.Bukkit;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;

/**
 * Created by basicer on 2/8/14.
 */
public class Metadata extends TCLCommand {

	@Override
	public String[] getArguments() { return new String[] { "-plugin=", "-all", "-remove", "object", "name", "value?" }; }

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Metadatable target = resolveObject(ctx.get("object"));
		String name = ctx.get("name").asString(ctx);
		Plugin p = ParchmentPluginLite.instance();

		if ( ctx.has("plugin") ) {
			p = Bukkit.getPluginManager().getPlugin(ctx.get("plugin").asString());
		}
		if ( p == null ) return EvaluationResult.makeError("Couldn't find requested plugin");

		if ( ctx.has("remove") ) {
			if ( ctx.has("all") ) {
				for ( MetadataValue v : target.getMetadata(name) ) {
					target.removeMetadata(name, v.getOwningPlugin());
				}
			} else {
				target.removeMetadata(name, p);
			}
		} if ( ctx.get("value") != null ) {
			Parameter value = ctx.get("value");
			target.setMetadata(name, new FixedMetadataValue(p, value));
			return EvaluationResult.makeOkay(value);
		}

		if ( ctx.has("all") ) {
			DictionaryParameter dict = new DictionaryParameter();
			for ( MetadataValue v : target.getMetadata(name) ) {
				dict.writeIndex(v.getOwningPlugin().getName(), fixUpMetadataValue(v));
			}
			return EvaluationResult.makeOkay(dict);
		}

		for ( MetadataValue v : target.getMetadata(name) ) {
			System.out.println(v.toString());
			if ( v.getOwningPlugin().equals(p) ) return EvaluationResult.makeOkay(fixUpMetadataValue(v));
		}

		return EvaluationResult.OK;

	}

	private Parameter fixUpMetadataValue(MetadataValue metadata) {
		Object o = metadata.value();
		if ( o instanceof Parameter ) return (Parameter) o;
		return Parameter.from(o);
	}

	private Metadatable resolveObject(Parameter object) {
		if ( object instanceof PlayerParameter ) return object.as(org.bukkit.entity.Player.class);
		if ( object instanceof EntityParameter ) return object.as(org.bukkit.entity.Entity.class);
		if ( object instanceof WorldParameter) return object.as(org.bukkit.World.class);
		if ( object instanceof BlockParameter) return object.as(org.bukkit.block.Block.class);
		if ( object instanceof LocationParameter) return object.as(org.bukkit.block.Block.class);
		throw new FizzleException("Couldn't convert object into something we can use metadata with: " + object.toString());
	}
}
