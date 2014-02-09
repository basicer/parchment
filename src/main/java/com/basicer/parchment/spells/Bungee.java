package com.basicer.parchment.spells;

import com.basicer.parchment.*;
import com.basicer.parchment.annotations.Operation;
import com.basicer.parchment.bukkit.ParchmentPluginLite;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.PlayerParameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitWorker;


import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 11/28/13
 * Time: 10:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bungee extends OperationalTCLCommand implements PluginMessageListener {

	final String channel = "BungeeCord";

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}

	private void init() {
		Messenger m = Bukkit.getServer().getMessenger();
		if ( !m.isOutgoingChannelRegistered(ParchmentPluginLite.instance(), channel) )
			m.registerOutgoingPluginChannel(ParchmentPluginLite.instance(), channel);

		if ( !m.isIncomingChannelRegistered(ParchmentPluginLite.instance(), channel) ) {
			m.registerIncomingPluginChannel(ParchmentPluginLite.instance(), channel, this);
		}
	}

	private byte[] craftBungeeMessage(String[] data) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);
		try {
			for ( String s : data ) out.writeUTF(s);
		} catch ( IOException ex ) {
			throw new FizzleException(ex.getMessage());
		}

		return b.toByteArray();
	}

	public Player getTransportPlayer(String hint) {
		if ( hint != null ) {
			Player pp = Bukkit.getServer().getPlayerExact(hint);
			if ( pp != null ) return pp;
		}
		return Bukkit.getServer().getOnlinePlayers()[0];
	}

	public Player getTransportPlayer() {
		return getTransportPlayer(null);
	}

	@Operation(aliases={"server","send"}, desc="Sent named player to a given server.")
	public Parameter connectOperation(Parameter dummy, Context ctx, StringParameter who, StringParameter server) {
		init();
		if ( server == null ) { who = ctx.getTarget().cast(StringParameter.class); server = who; }
		getTransportPlayer(who.asString(ctx)).sendPluginMessage(ParchmentPluginLite.instance(), channel, craftBungeeMessage(new String[]{"ConnectOther", who.asString(ctx), server.asString(ctx)}));
		return Parameter.EmptyString;
	}


	@Operation(aliases={"tell", "msg"}, desc="Sent chat message to named player.")
	public Parameter messageOperation(Parameter dummy, Context ctx, StringParameter wo, StringParameter what) {
		init();
		Player p = wo.as(Player.class, ctx);
		if ( p == null ) p = getTransportPlayer();
		p.sendPluginMessage(ParchmentPluginLite.instance(), channel, craftBungeeMessage(new String[]{"Message", wo.asString(ctx), what.asString(ctx)}));
		return Parameter.EmptyString;
	}

	private class Holder<T> {
		T value;
	}

	private interface ResultCreator {
		Parameter createResult(ArrayList<String> in);
	}

	@Operation()
	public EvaluationResult ipOperation(final Parameter dummy, final Context ctx, final PlayerParameter who) {
		init();
		return genericQuery(who.asPlayer(ctx), craftBungeeMessage(new String[]{"IP"}), new ResultCreator() {
			public Parameter createResult(ArrayList<String> in) {
				return Parameter.from(in.get(1));
			}
		});
	}

	@Operation()
	public EvaluationResult playerlistOperation(final Parameter dummy, final Context ctx, StringParameter srv) {
		init();
		final String server = (srv != null ? srv.asString(ctx) : "ALL");
		return genericQuery(getTransportPlayer(), craftBungeeMessage(new String[]{"PlayerList", server}), new ResultCreator() {
			public Parameter createResult(ArrayList<String> in) {
				return commaStringToList(in.get(2));
			}
		});
	}



	@Operation()
	public EvaluationResult serverListOperation(final Parameter dummy, final Context ctx) {
		init();
		return genericQuery(getTransportPlayer(), craftBungeeMessage(new String[]{"GetServers"}), new ResultCreator() {
			public Parameter createResult(ArrayList<String> in) {
				return commaStringToList(in.get(1));
			}
		});
	}

	@Operation()
	public EvaluationResult getServerOperation(final Parameter dummy, final Context ctx) {
		init();
		return genericQuery(getTransportPlayer(), craftBungeeMessage(new String[]{"GetServer"}), new ResultCreator() {
			public Parameter createResult(ArrayList<String> in) {
				return Parameter.from(in.get(1));
			}
		});
	}

	private Parameter commaStringToList(String s) {
		String[] lst = s.split(", ");
		ArrayList<Parameter> result = new ArrayList<Parameter>();
		for ( String ss : lst ) result.add(Parameter.from(ss));
		return ListParameter.from(result);


	}

	private EvaluationResult genericQuery(final Player p, final byte[] odata, final ResultCreator creator) {
		final Holder<Integer> state = new Holder<Integer>();
		state.value = 0;
		return new BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
				switch ( state.value.intValue() ) {
					case 0:
						if ( waiting ) return new BranchEvaluationResult(null, null, this);
						waiting = false;
						state.value = 1;
						p.sendPluginMessage(ParchmentPluginLite.instance(), channel, odata);
						return new BranchEvaluationResult(null, null, this);
					case 1:
						if ( data == null ) return last;
						DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
						data = null;
						waiting = false;
						ArrayList<String> strings = new ArrayList<String>();

						String s = null;
						try {
							while ( (s = in.readUTF()) != null ) strings.add(s);
						} catch ( IOException ex ) {  }

						return EvaluationResult.makeOkay(creator.createResult(strings));
					default:
						throw new FizzleException("WTF, Wrong State?");
				}
			}
		});
	}

	boolean waiting = false;
	byte[] data = null;

	public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
		data = bytes;
	}
}
