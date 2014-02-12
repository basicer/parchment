package com.basicer.parchment.extra;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.*;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by basicer on 2/11/14.
 */
public class Redis extends OperationalTCLCommand {

	private Jedis client;

	protected Jedis getClient() {
		if ( client == null ) client = new Jedis("localhost");
		if ( !client.isConnected() ) {
			try {
				client.connect();
			} catch (UnknownHostException e) {
				throw new FizzleException(e.getMessage());
			} catch ( IOException e ) {
				throw new FizzleException(e.getMessage());
			}
		}
		return client;
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}

	public String stringOrFizzle(Parameter p, Context ctx) {
		if ( p == null ) throw new FizzleException("Required parameter missing");
		return p.asString(ctx);
	}

	public String packValue(Parameter p, Context ctx) {
		return p.asString(ctx);
	}

	public Parameter unpackValue(String s) {
		return StringParameter.from(s);
	}

	public Parameter setOperation(Parameter dummy, Context ctx, StringParameter key, Parameter value) {
		String skey = stringOrFizzle(key, ctx);
		return StringParameter.from(getClient().set(skey, packValue(value, ctx)));
	}

	public Parameter getOperation(Parameter dummy, Context ctx, StringParameter key) {
		String skey = stringOrFizzle(key, ctx);
		return unpackValue(getClient().get(skey));
	}

	public Parameter delOperation(Parameter dummy, Context ctx, StringParameter key) {
		String skey = stringOrFizzle(key, ctx);
		return BooleanParameter.from(getClient().del(skey));
	}


	public Parameter incrOperation(Parameter dummy, Context ctx, StringParameter key, IntegerParameter ammt) {
		String skey = stringOrFizzle(key, ctx);
		return IntegerParameter.from(getClient().incrBy(skey, ammt == null ? 1 : ammt.asInteger(ctx)));
	}
	public Parameter decrOperation(Parameter dummy, Context ctx, StringParameter key, IntegerParameter ammt) {
		String skey = stringOrFizzle(key, ctx);
		return IntegerParameter.from(getClient().decrBy(skey, ammt == null ? 1 : ammt.asInteger(ctx)));
	}

	// List

	public Parameter llenOperation(Parameter dummy, Context ctx, StringParameter key) {
		String skey = stringOrFizzle(key, ctx);
		return IntegerParameter.from(getClient().llen(skey));
	}


	// Hashes

	public Parameter hgetOperation(Parameter dummy, Context ctx, StringParameter key, StringParameter field) {
		String skey = stringOrFizzle(key, ctx);
		String sfield = stringOrFizzle(field, ctx);
		return StringParameter.from(getClient().hget(skey, sfield));
	}

	public Parameter hsetOperation(Parameter dummy, Context ctx, StringParameter key, StringParameter field, Parameter value) {
		String skey = stringOrFizzle(key, ctx);
		String sfield = stringOrFizzle(field, ctx);
		return StringParameter.from(getClient().hset(skey, sfield, packValue(value, ctx)));
	}

	public Parameter hexistsOperation(Parameter dummy, Context ctx, StringParameter key, StringParameter field, Parameter value) {
		String skey = stringOrFizzle(key, ctx);
		String sfield = stringOrFizzle(field, ctx);
		return BooleanParameter.from(getClient().hexists(skey, sfield));
	}


	public Parameter hgetallOperation(Parameter dummy, Context ctx, StringParameter key) {
		String skey = stringOrFizzle(key, ctx);
		java.util.Map<String, String> keys = getClient().hgetAll(skey);
		DictionaryParameter result = new DictionaryParameter();
		for ( String s : keys.keySet() ) {
			result.writeIndex(s, unpackValue(keys.get(s)));
		}

		return result;
	}



}
