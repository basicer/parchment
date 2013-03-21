package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.EvaluationResult.BranchEvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.LazyParameter;
import com.basicer.parchment.parameters.Parameter;

public class TCLEngine {

	PushbackReader	sourcecode;
	Context			ctx;

	public TCLEngine(String src, Context ctx) {
		sourcecode = new PushbackReader(new StringReader(src));
		this.ctx = ctx;
	}

	public TCLEngine(PushbackReader src, Context ctx) {
		sourcecode = src;
		this.ctx = ctx;
	}

	private EvaluationResult	result	= EvaluationResult.OK;

	public boolean step() {
		if (result instanceof EvaluationResult.BranchEvaluationResult) {
			EvaluationResult.BranchEvaluationResult br = (EvaluationResult.BranchEvaluationResult) result;
				try {
					if (br.getToRun() != null) result = cheatyEvaluate(br.getToRun(), br.getContext());
					if (!(result instanceof EvaluationResult.BranchEvaluationResult) && result.getCode() == Code.ERROR) {
						ctx.top().put("errorInfo", result.getValue());
					}
					result = br.invokeCallback(result);
					return true;
				} catch (FizzleException ex) {
					result = EvaluationResult.makeError(ex.getMessage());
					return true;
				}
		} else if (result.getCode() != Code.OK) {
			if (result.getCode() == Code.ERROR) {
				ctx.top().put("errorInfo", result.getValue());
			}
			return false;
		}

		
		Parameter[] pargs = null;
		try {
			pargs = parseLine(sourcecode, ctx);
		} catch (FizzleException ex) { //This should be needed
			result = EvaluationResult.makeError(ex.getMessage()); 
			return true;
		}

		if (pargs == null) return false;
		if (pargs.length < 1) return true;
		
		for ( int pi = 0; pi < pargs.length; ++pi) {
			Parameter r = pargs[pi];
			if ( r instanceof LazyParameter ) {
				LazyParameter lr = (LazyParameter) r;
				
				
				EvaluationResult ler = lr.resolve();				
				if ( ler.getCode() != Code.RETURN && ler.getCode() != Code.OK ) { result = ler; return true; }
				pargs[pi] = ler.getValue();
			}
		}

		String name = pargs[0].asString();
		TCLCommand s = ctx.getCommand(name);
		if (s == null) { result = EvaluationResult.makeError("No such command: " + name); return true; }
		try {
			Context c2 = s.bindContext(pargs, ctx);
			result = s.extendedExecute(c2, this);
			return true;
		} catch (FizzleException ex) {
			result = EvaluationResult.makeError(ex.getMessage());
			return true;
		}
		
		

	}

	/*
	private EvaluationResult evaluate(String s, Context ctx) {
		return evaluate(new PushbackReader(new StringReader(s)), ctx);
	}

	
	private EvaluationResult evaluate(PushbackReader r, Context ctx) {
		EvaluationResult result = null;
		while (true) {
			Parameter[] pargs;
			try {
				pargs = parseLine(r, ctx);
			} catch (FizzleException ex) {
				return EvaluationResult.makeError(ex.getMessage());
			}
			if (pargs == null) break;
			if (pargs.length < 1) continue;
			// for (Parameter p : pargs) {
			// ctx.sendDebugMessage("[P] " + p.toString());
			// }

			result = evaluate(pargs, ctx);
			while (result instanceof EvaluationResult.BranchEvaluationResult) {
				EvaluationResult.BranchEvaluationResult br = (EvaluationResult.BranchEvaluationResult) result;
				result = evaluate(br);
			}
			if (result.getCode() != Code.OK) return result;
			// ctx.sendDebugMessage("[R] " + result.toString());
		}
		return result;
	}
	*/
	

	public static EvaluationResult cheatyEvaluate(String toRun, Context context) {
		TCLEngine e = new TCLEngine(toRun, context);
		while ( e.step() ) {}
		return new EvaluationResult(e.getResult(), e.getCode());
	}

	/*
	private EvaluationResult evaluate(Parameter[] pargs, Context ctx) {
		String name = pargs[0].asString();
		TCLCommand s = ctx.getCommand(name);
		if (s == null) return EvaluationResult.makeError("No such command: " + name);
		try {
			Context c2 = s.bindContext(pargs, ctx);
			EvaluationResult result = s.extendedExecute(c2, this);

			return result;
		} catch (FizzleException ex) {
			return EvaluationResult.makeError(ex.getMessage());
		}

	}
	 */
	
	// Doesnt evaluate if there is no engine
/*
	private static Parameter evaulateVariable(PushbackReader s, Context ctx, TCLEngine engine) throws IOException {

		if (engine == null)
			return Parameter.from("$" + TCLUtils.readVariableName(s, ctx));
		else
			return TCLUtils.evaulateVariable(s, ctx);

	}
*/
	
	/*
	// Doesnt evaluate if there is no engine
	public static Parameter evaulateBracketExpression(PushbackReader s, Context ctx, TCLEngine engine)
			throws IOException {
		StringBuilder cmd = new StringBuilder();
		TCLUtils.readBracketExpression(s, cmd);
		if (engine == null) return Parameter.from("[" + cmd + "]");
		return engine.evaluate(cmd.toString(), ctx).getValue();
	}
	 
	 */
	
	public Parameter[] parseLine(PushbackReader s, Context ctx) {
		return parseLine(s, ctx, this);
	}

	// Doesnt evaluate if there is no engine
	public static Parameter[] parseLine(PushbackReader s, Context ctx, TCLEngine engine) {
		List<Parameter> out = new ArrayList<Parameter>();
		StringBuilder current = new StringBuilder();
		char in = '\0';
		boolean empty = true;
		Parameter currentp = null;
		boolean at_end = true;
		int r;
		try {
			while ((r = s.read()) > 0) {
				char c = (char) r;

				boolean append = false;
				if (in == '"') {
					if (c == '\\') {
						current.append(TCLUtils.readSlashCode(s));
						empty = false;
					} else if (c == '"') {
						in = '\0';
						int xcn = s.read();
						if (xcn > 0) {
							s.unread(xcn);

							if (!Character.isWhitespace(xcn) && (char) xcn != ';') {
								System.err.println("Bad : " + xcn + " " + ((char) xcn));
								System.err.println("-> |" + current.toString() + "|");
								System.err.println("SoFar ");
								for (Parameter p : out)
									System.err.println(p.toString());
								throw new FizzleException("extra characters after close-quote");
							}

						}
					} else if (c == '{' && false) {
						s.unread(r);
						TCLUtils.readCurlyBraceString(s, current);
						empty = false;
					} else if (c == '[') {
						s.unread(r);
						currentp = new LazyParameter(LazyParameter.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
					} else if (c == '$') {
						s.unread(r);
						// TODO : We might want to force this to be a string
						Parameter var = new LazyParameter(LazyParameter.Type.VARIABLE, TCLUtils.readVariableName(s, ctx), ctx);
						if (currentp != null) {
							current.append(currentp.asString());
							empty = false;
							currentp = null;
						}
						if (empty)
							currentp = var;
						else
							current.append(var.asString());
					} else {
						append = true;
					}
				} else {
					if (c == '\\') {
						current.append(TCLUtils.readSlashCode(s));
						empty = false;
					} else if (c == '"' && empty)
						in = c;
					else if (c == '{' && empty) {
						s.unread(r);
						TCLUtils.readCurlyBraceString(s, current);
						empty = false;
					} else if (c == '[') {
						s.unread(r);
						currentp = new LazyParameter(LazyParameter.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
					} else if (c == ' ' || c == '\t' || c == '\r' || c == (char) 11) {
						if (currentp != null) {
							currentp.asString(ctx);  //TODO: Remove this as it goes against everything we stand for.
							out.add(currentp);
							currentp = null;
						} else if (!empty) {
							out.add(Parameter.from(current.toString()));
						}
						current.setLength(0);
						empty = true;
					} else if (c == '\n' || c == ';') {
						at_end = false;
						break;
					} else if (c == '$') {
						s.unread(r);
						String name = TCLUtils.readVariableName(s, ctx);
						if ( name.length() > 0 ) {
							Parameter var = new LazyParameter(LazyParameter.Type.VARIABLE, name, ctx);
							
							if (currentp != null) {
								current.append(currentp.asString());
								empty = false;
								currentp = null;
							}
							if (empty)
								currentp = var;
							else
								current.append(var.asString());
						} else {
							append = true;
						}
					} else if (c == '#' && currentp == null && current.length() < 1 && out.size() < 1) {
						while (c != '\n') {
							r = s.read();
							if (r < 0) return null;
							c = (char) r;
						}
						return new Parameter[0];
					} else {
						append = true;
					}
				}
				// if (currentp != null && !empty) {
				// current.append(currentp.asString());
				// currentp = null;
				// empty = false;
				// }
				if (append) {
					if (currentp != null) {
						current.append(currentp.asString(ctx));
						currentp = null;
					}

					current.append(c);

					empty = false;
				}

			}

			if (currentp != null && current.length() == 0) {
				currentp.asString(ctx);  //TODO: Remove this as it goes against everything we stand for.
				out.add(currentp);
			} else if (!empty) {
				if (currentp != null) current.append(currentp.asString(ctx));
				out.add(Parameter.from(current.toString()));
			}

		} catch (IOException e) {
			throw new Error(e);
		}

		if (at_end && out.size() < 1) return null;
		return out.toArray(new Parameter[0]);
	}

	public Parameter getResult() {
		// TODO Auto-generated method stub
		if (result == null) return null;
		return result.getValue();
	}

	public Code getCode() {
		// TODO Auto-generated method stub
		if (result == null) return null;
		return result.getCode();
	}

}
