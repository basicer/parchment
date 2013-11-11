package com.basicer.parchment.tclutil;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.AsyncCharConsumer;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.protocol.HttpContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Http extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "-body", "-convert", "-timeout=", "url" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		Parameter to = ctx.get("timeout");

		final String surl = ctx.get("url").asString(ctx);
		final int timeout = (to == null) ? 0 : to.asInteger(ctx);
		final boolean only_body = ctx.has("body") ? ctx.get("body").asBoolean() : false;
		final boolean convert = ctx.has("convert") ? ctx.get("convert").asBoolean() : false;

		try {
			URL url = new URL(surl);
		} catch ( MalformedURLException muex ) {
			return new EvaluationResult(Parameter.from("malformed url: " + muex.getMessage()), EvaluationResult.Code.ERROR);
		}

		final CloseableHttpAsyncClient httpclient = HttpAsyncClients.createDefault();
		final long start = System.currentTimeMillis();

		httpclient.start();
		final Future<Parameter> result = httpclient.execute(HttpAsyncMethods.createGet(surl), new AsyncCharConsumer<Parameter>() {
			private StringBuffer str = new StringBuffer();
			private DictionaryParameter out;
			@Override
			protected void onCharReceived(CharBuffer charBuffer, IOControl ioControl) throws IOException {
				while (charBuffer.hasRemaining() ) str.append(charBuffer.get());
			}

			@Override
			protected void onResponseReceived(HttpResponse httpResponse) throws HttpException, IOException {
				out = new DictionaryParameter();
				out.writeIndex("http", Parameter.from(httpResponse.getStatusLine().toString()));
				out.writeIndex("url", Parameter.from(surl));
				if ( httpResponse.getEntity().getContentType() != null ) {
					Parameter ct = Parameter.from(httpResponse.getEntity().getContentType().getValue());
					out.writeIndex("type",ct);
				}

				if ( httpResponse.getEntity().getContentEncoding() != null ) {
					Parameter ce = Parameter.from(httpResponse.getEntity().getContentEncoding().getValue());
					out.writeIndex("encoding",ce);
				}
			}

			@Override
			protected Parameter buildResult(HttpContext httpContext) throws Exception {

				Parameter body = null;
				if ( !convert ) {
					body = Parameter.from(str.toString());
				} else {
					//Detect converstion type here, but JSON for now.
					body = JSONToTCL(JSONValue.parse(str.toString()));
				}
				out.writeIndex("currentsize", Parameter.from(str.length()));
				out.writeIndex("body", body);
				if ( only_body ) return body;
				else return out;

			}
		}, null);


		return new EvaluationResult.BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
				EvaluationResult.EvalCallback rerun = this;
				if ( timeout > 0 && (System.currentTimeMillis() - start) > timeout ) {
					try {
						httpclient.close();
					} catch ( Exception ex ) {

					}
					return new EvaluationResult(Parameter.from("Timeout"), EvaluationResult.Code.ERROR);
				}
				if ( !result.isDone() ) return new EvaluationResult.BranchEvaluationResult(null, null, rerun) ;

				try {
					httpclient.close();
				} catch (IOException e1) {

				}
				try {
					return new EvaluationResult(result.get());
				} catch (InterruptedException e1) {
					return new EvaluationResult(Parameter.from(e1.getMessage()), EvaluationResult.Code.ERROR);
				} catch (ExecutionException e1) {
					return new EvaluationResult(Parameter.from(e1.getMessage()), EvaluationResult.Code.ERROR);
				}


			}
		});

		//return new EvaluationResult(Parameter.from("rob"));

	}

	private static Parameter JSONToTCL(Object o) {
		if ( o == "null" ) return Parameter.from("null");
		else if ( o instanceof JSONObject) {
			JSONObject oo = (JSONObject) o;

			DictionaryParameter ap = new DictionaryParameter();
			for ( Object k : oo.keySet() ) {
				ap.writeIndex((String) k, JSONToTCL(oo.get(k)));
			}
			return ap;
		} else if ( o instanceof JSONArray ) {
			JSONArray ao = (JSONArray) o;
			ArrayList<Parameter> lout = new ArrayList<Parameter>();
			for ( Object v : ao ) {
				lout.add(JSONToTCL(v));
			}
			return ListParameter.from(lout);
		} else if ( o instanceof String ) {
			return Parameter.from((String) o);
		} else if ( o instanceof Long ) {
			return Parameter.from(((Long) o).intValue());
		} else if ( o instanceof Boolean ) {
			return Parameter.from(((Boolean) o).booleanValue());
		} else if ( o instanceof Double ) {
			return Parameter.from((Double) o);
		} else {
			return Parameter.from(o.getClass().getName());
		}
	}


}