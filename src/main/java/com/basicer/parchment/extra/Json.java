package com.basicer.parchment.extra;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tcl.OperationalTCLCommand;
import com.basicer.parchment.tclutil.Http;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.*;

/**
 * Created by basicer on 2/16/14.
 */
public class Json extends OperationalTCLCommand {

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		return basicExtendedExecute(ctx, e);
	}

	public Parameter decodeOperation(Parameter dummy, Context ctx, Parameter text) {
		JsonParser parser = new JsonParser();
		return JSONToTCL(parser.parse(text.asString(ctx).toString()));
	}


	public static Parameter JSONToTCL(String s){
		JsonReader r = new JsonReader(new StringReader(s));
		r.setLenient(true);
		return Json.JSONToTCL(new JsonParser().parse(r));
	}

	public static Parameter JSONToTCL(JsonElement o) {
		if ( o instanceof JsonNull) return Parameter.from("null");
		else if ( o instanceof JsonObject) {
			JsonObject oo = (JsonObject) o;

			DictionaryParameter ap = new DictionaryParameter();
			for ( java.util.Map.Entry<String, JsonElement> e : oo.entrySet() ) {
				ap.writeIndex(e.getKey(), JSONToTCL(e.getValue()));
			}
			return ap;
		} else if ( o instanceof JsonArray) {
			JsonArray ao = (JsonArray) o;
			ArrayList<Parameter> lout = new ArrayList<Parameter>();
			for ( JsonElement v : ao ) {
				lout.add(JSONToTCL(v));
			}
			return ListParameter.from(lout);
		} else if ( o instanceof JsonPrimitive) {
			JsonPrimitive p = (JsonPrimitive) o;
			if ( p.isBoolean() ) return Parameter.from(p.getAsBoolean());
			else if ( p.isNumber() )  {
				Number n = p.getAsNumber();
				if ( n.doubleValue() == n.longValue() ) return Parameter.from(n.longValue());
				return Parameter.from(n.doubleValue());
			} else {
				return Parameter.from(o.getAsString());
			}
		} else {
			return Parameter.from(o.getClass().getName());
		}
	}

}
