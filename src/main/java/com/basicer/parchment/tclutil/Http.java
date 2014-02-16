package com.basicer.parchment.tclutil;

import com.basicer.parchment.*;
import com.basicer.parchment.extra.Json;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.IntegerParameter;
import com.basicer.parchment.parameters.ListParameter;
import com.basicer.parchment.parameters.Parameter;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.gson.JsonParser;
import org.apache.http.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.*;
import org.apache.http.protocol.*;


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
			return EvaluationResult.makeError("malformed url: " + muex.getMessage());
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
					JsonParser parser = new JsonParser();
					body = Json.JSONToTCL(parser.parse(str.toString()));
				}
				out.writeIndex("currentsize", Parameter.from(str.length()));
				out.writeIndex("body", body);
				if ( only_body ) return body;
				else return out;

			}
		}, null);


		return new BranchEvaluationResult(null, null, new EvaluationResult.EvalCallback() {
			public EvaluationResult result(EvaluationResult last) {
				EvaluationResult.EvalCallback rerun = this;
				if ( timeout > 0 && (System.currentTimeMillis() - start) > timeout ) {
					try {
						httpclient.close();
					} catch ( Exception ex ) {

					}
					return EvaluationResult.makeError(Parameter.from("Timeout"));
				}
				if ( !result.isDone() ) return new BranchEvaluationResult(null, null, rerun) ;

				try {
					httpclient.close();
				} catch (IOException e1) {

				}
				try {
					return EvaluationResult.makeOkay(result.get());
				} catch (InterruptedException e1) {
					return EvaluationResult.makeError(e1.getMessage());
				} catch (ExecutionException e1) {
					return EvaluationResult.makeError(e1.getMessage());
				}


			}
		});

		//return new EvaluationResult(Parameter.from("rob"));

	}




}