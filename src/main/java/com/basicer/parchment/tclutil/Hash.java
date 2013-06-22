package com.basicer.parchment.tclutil;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Hash extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "type", "data" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String scheme = ctx.get("type").asString(ctx).toUpperCase();
		String data = ctx.get("data").asString(ctx);
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(scheme);
		} catch (NoSuchAlgorithmException e1) {
			return EvaluationResult.makeError("No such hasing scheme known: " + scheme);
		}

		byte[] hash;
		try {
			hash = digest.digest(data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			return EvaluationResult.makeError("No such string encoding scheme known: UTF-8");
		}

		StringBuffer hexString = new StringBuffer();

		for ( int i = 0; i < hash.length; i++ ) {
			String hex = Integer.toHexString(0xff & hash[i]);
			if ( hex.length() == 1 ) hexString.append('0');
			hexString.append(hex);
		}

		return new EvaluationResult(Parameter.from(hexString.toString()));

	}

}