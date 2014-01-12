package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.basicer.parchment.EvaluationResult.BranchEvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.bukkit.ParchmentPlugin;
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tclstrings.ErrorStrings;
import com.google.common.base.Function;

import javax.security.auth.callback.Callback;

public class TCLEngine {

	String last_ran_code;
	PushbackReader sourcecode;
	Context ctx;
	private EvaluationResult result = EvaluationResult.OK;
	private TCLEngine sub;
	private BranchEvaluationResult subbr;
	public boolean resilient = false;
	public Function<Callable<EvaluationResult>, EvaluationResult> commandGuard = null;

	public TCLEngine(String src, Context ctx) {
		sourcecode = new PushbackReader(new StringReader(src), 2);
		this.ctx = ctx;
	}

	public TCLEngine(Reader s, Context ctx) {
		if ( !(s instanceof PushbackReader)) s = new PushbackReader(s, 2);

		PushbackReader ps = (PushbackReader) s;
		sourcecode = ps;
		this.ctx = ctx;
	}
	
	public TCLEngine(EvaluationResult er, Context ctx) {
		result = er;
		this.ctx = ctx;
	}
	
	public TCLEngine(BranchEvaluationResult br) {
		result = br;
		this.ctx = br.getContext();
	}

	ParameterAccumulator[] pargs = null;

	public boolean step() {
		return step(false);
	}
	
	public boolean step(boolean allow_sleeping) {
		if ( sub != null ) {
			if ( sub.step() ) return true;
			result = sub.getEvaluationResult();
			sub = null;

			if ( !(result instanceof EvaluationResult.BranchEvaluationResult) && result.getCode() == Code.ERROR ) {
				if ( result.getRefrencedCode() == null ) result.setRefrencedCode(last_ran_code);
				String s = result.getValue().asString() + "\n    while executing\n\"" + result.getRefrencedCode() + "\"";
				ctx.top().put("errorInfo", StringParameter.from(s));
			}

			return true;
		}

		if ( subbr != null ) {
			last_ran_code = subbr.getToRun();
			try {
				//To user land.
				result = subbr.invokeCallback(result);
			} catch (FizzleException ex) {
				result = EvaluationResult.makeError(ex.getMessage());
			}
			subbr = null;
			return true;
		}

		if ( result instanceof EvaluationResult.BranchEvaluationResult ) {
			
			EvaluationResult.BranchEvaluationResult br = (EvaluationResult.BranchEvaluationResult) result;
			Long when = br.getScheduleAfter();
			if ( when != null && when > System.currentTimeMillis() && allow_sleeping ) return true;
			if ( br.getToRun() != null ) {
				sub = new TCLEngine(br.getToRun(), br.getContext());
				sub.commandGuard = commandGuard;
			}
			last_ran_code = br.getToRun();
			subbr = br;
			return true;
		} else if ( result.getCode() != Code.OK ) {
			if ( result.getCode() == Code.ERROR ) {
				ctx.top().put("errorInfo", result.getValue());
			}
			if ( !resilient ) return false;
		}

		if ( pargs != null ) {
			for ( int pi = 0; pi < pargs.length; ++pi ) {
				ParameterAccumulator r = pargs[pi];
				if ( !r.isResolved() ) {
					//To User Land
					r.resolveStep();
					return true;
				}
				if ( r.getEvaluationResult().getCode() == Code.ERROR ) {
					result = r.getEvaluationResult();
					if (resilient) {
						pargs = null;
						return true;
					} else {
						return false;
					}
				}
			}

			Parameter[] rpargs = new Parameter[pargs.length];
			for ( int i = 0; i < pargs.length; ++i ) {
				rpargs[i] = pargs[i].getEvaluationResult().getValue();
			}
			
			//To User Land
			String name = rpargs[0].asString();

			final TCLCommand s = ctx.getCommand(name);
			final TCLEngine that = this;
			if ( s == null ) {
				pargs = null;
				result = EvaluationResult.makeError(String.format(ErrorStrings.NoCommand, name));
				return true;
			}
			try {
				//To User Land (Calls asString())
				final Context c2 = s.bindContext(rpargs, ctx);

				//To User Land
				if ( commandGuard == null ) {
					result = s.extendedExecute(c2, this);
				} else {
					result = commandGuard.apply(new Callable<EvaluationResult>() {
						@Override
						public EvaluationResult call() throws Exception {
							return s.extendedExecute(c2, that);
						}
					});
				}
				pargs = null;
				return true;
			} catch (FizzleException ex) {
				result = EvaluationResult.makeError(ex.getMessage());
				pargs = null;
				return true;
			}
		}

		if ( sourcecode == null ) return false; //We where evaluating with result given.
		
		try {
			pargs = parseLine(sourcecode, ctx);
		} catch (FizzleException ex) {
			result = EvaluationResult.makeError(ex.getMessage());
			return true;
		}

		if ( pargs == null ) return false;
		if ( pargs.length < 1 ) {
			pargs = null;
			return true;
		}

		return true;

	}

	public EvaluationResult getEvaluationResult() {
		return this.result;
	}

	/*
	 * private EvaluationResult evaluate(String s, Context ctx) { return
	 * evaluate(new PushbackReader(new StringReader(s)), ctx); }
	 * 
	 * 
	 * private EvaluationResult evaluate(PushbackReader r, Context ctx) {
	 * EvaluationResult result = null; while (true) { Parameter[] pargs; try {
	 * pargs = parseLine(r, ctx); } catch (FizzleException ex) { return
	 * EvaluationResult.makeError(ex.getMessage()); } if (pargs == null) break;
	 * if (pargs.length < 1) continue; // for (Parameter p : pargs) { //
	 * ctx.sendDebugMessage("[P] " + p.toString()); // }
	 * 
	 * result = evaluate(pargs, ctx); while (result instanceof
	 * EvaluationResult.BranchEvaluationResult) {
	 * EvaluationResult.BranchEvaluationResult br =
	 * (EvaluationResult.BranchEvaluationResult) result; result = evaluate(br);
	 * } if (result.getCode() != Code.OK) return result; //
	 * ctx.sendDebugMessage("[R] " + result.toString()); } return result; }
	 */

	/*
	public static EvaluationResult cheatyEvaluate(String toRun, Context context) {
		TCLEngine e = new TCLEngine(toRun, context);
		while ( e.step() ) {
		}
		return new EvaluationResult(e.getResult(), e.getCode());
	}
	*/

	/*
	 * private EvaluationResult evaluate(Parameter[] pargs, Context ctx) {
	 * String name = pargs[0].asString(); TCLCommand s = ctx.getCommand(name);
	 * if (s == null) return EvaluationResult.makeError("No such command: " +
	 * name); try { Context c2 = s.bindContext(pargs, ctx); EvaluationResult
	 * result = s.extendedExecute(c2, this);
	 * 
	 * return result; } catch (FizzleException ex) { return
	 * EvaluationResult.makeError(ex.getMessage()); }
	 * 
	 * }
	 */

	// Doesnt evaluate if there is no engine
	/*
	 * private static Parameter evaulateVariable(PushbackReader s, Context ctx,
	 * TCLEngine engine) throws IOException {
	 * 
	 * if (engine == null) return Parameter.from("$" +
	 * TCLUtils.readVariableName(s, ctx)); else return
	 * TCLUtils.evaulateVariable(s, ctx);
	 * 
	 * }
	 */

	/*
	 * // Doesnt evaluate if there is no engine public static Parameter
	 * evaulateBracketExpression(PushbackReader s, Context ctx, TCLEngine
	 * engine) throws IOException { StringBuilder cmd = new StringBuilder();
	 * TCLUtils.readBracketExpression(s, cmd); if (engine == null) return
	 * Parameter.from("[" + cmd + "]"); return engine.evaluate(cmd.toString(),
	 * ctx).getValue(); }
	 */

	public static ParameterAccumulator[] parseLine(PushbackReader s, Context ctx) {
		return  parseLine(s, ctx, false);
	}

	public static ParameterAccumulator[] legacyParseLine(PushbackReader s, Context ctx, boolean expr) {

		final String exprSymbols = "-+*/%=<>^&|!";
		List<ParameterAccumulator> out = new ArrayList<ParameterAccumulator>();

		char in = '\0';
		ParameterAccumulator current = new ParameterAccumulator();
		boolean at_end = true;
		boolean did_expr_match = false;
		int r;
		try {
			while ( (r = s.read()) > 0 ) {
				char c = (char) r;
				boolean append = false;
				if ( in == '"' ) {
					if ( c == '\\' ) {
						current.append(TCLUtils.readSlashCode(s));
					} else if ( c == '"' ) {
						in = '\0';
						int xcn = s.read();
						if ( xcn > 0 ) {
							int xcnn = s.read();
							if ( xcnn > 0 ) s.unread(xcnn);
							s.unread(xcn);

							//If right after a close quote we try to eat a new line, thats okay.
							//TODO: This wount throw the correct error for something like "\\
							if ( xcn == '\\' ) xcn = xcnn;


							if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
								System.err.println("Bad : " + xcn + " " + ((char) xcn));
								System.err.println("-> |" + current.toString() + "|");
								System.err.println("SoFar ");
								for ( ParameterAccumulator p : out )
									System.err.println(p.toString());

								if ( !expr || exprSymbols.indexOf(xcn) == -1 ) throw new FizzleException("extra characters after close-quote");
							}

						}
					} else if ( c == '[' ) {
						s.unread(r);
						current.append(ParameterAccumulator.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
					} else if ( c == '$' ) {
						s.unread(r);
						String name = TCLUtils.readVariableName(s, ctx);
						if ( name.length() > 0 ) {
							current.append(ParameterAccumulator.Type.VARIABLE, name, ctx);
						} else {
							append = true;
						}
					} else {
						append = true;
					}
				} else {
					if ( c == '\\' ) {
						String ta = TCLUtils.readSlashCode(s);
						if ( !ta.equals("") ) {
							current.append(ta);
							continue;
						} else {
							c = ' ';
						}
					}

					if ( c == '"' && current.empty() ) {
						in = c;
					} else if ( c == '{' && current.empty() ) {
						s.unread(r);
						current.append(TCLUtils.readCurlyBraceStringToString(s));
						int xcn = s.read();
						if ( xcn > 0 ) {
							s.unread(xcn);
							if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
								throw new FizzleException("extra characters after close-brace");
							}
						}
					} else if ( c == '[' ) {
						s.unread(r);
						current.append(ParameterAccumulator.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
					} else if ( c == ' ' || c == '\t' || c == '\r' || c == (char) 11 || c == '\f' ) {
						if ( !current.empty() ) {
							out.add(current);
							current = new ParameterAccumulator();
						}
					} else if ( c == '\n' || c == ';' ) {
						at_end = false;
						break;
					} else if ( c == '$' ) {
						s.unread(r);
						String name = TCLUtils.readVariableName(s, ctx);
						if ( name.length() > 0 ) {
							current.append(ParameterAccumulator.Type.VARIABLE, name, ctx);
						} else {
							append = true;
						}
					} else if ( c == '#' && current.empty() && out.size() < 1 ) {
						boolean before_slash = false;

						while ( c != '\n' || before_slash ) {
							r = s.read();
							if ( r < 0 ) return null;

							//Account for an escaped endline, which continues the comment
							if ( before_slash ) before_slash = false;
							else if ( c == '\\' ) before_slash = true;
							c = (char) r;
						}
						return new ParameterAccumulator[0];
					} else if ( expr && exprSymbols.indexOf(c) != -1 && !did_expr_match ) {
						if ( !current.empty() ) {
							out.add(current);
						}

						ParameterAccumulator op = new ParameterAccumulator();
						op.append("" + c);
						int i;
						for ( i = s.read(); i != -1 && exprSymbols.indexOf(i) != -1; i = s.read() ) op.append("" + (char)i);
						if ( i > 0 ) s.unread(i);

						out.add(op);
						current = new ParameterAccumulator();
						did_expr_match = true;
					} else {
						append = true;
					}
				}

				if ( append ) {
					did_expr_match = false;
					current.append("" + c);
				}

			}

			if ( !current.empty() ) out.add(current);

		} catch (IOException e) {
			throw new FizzleException(e.getMessage());
		}

		if ( at_end && out.size() < 1 ) return null;
		if ( in != '\0' ) throw new FizzleException("missing " + in);
		return out.toArray(new ParameterAccumulator[0]);
	}

	public static ParameterAccumulator[] parseLine(PushbackReader s, Context ctx, boolean expr) {
		ArrayList<ParameterAccumulator> out = new ArrayList<>();
		boolean first = true;
		do {
			try {
				ParameterAccumulator read = parseWord(s, ctx, first);
				if ( read == null ) {
					if ( read == null && out.size() == 0 ) return null;
//					for ( int i = 0; i < out.size(); ++i ) {
//						System.out.println(" " + i + " :" + out.get(i).toString());
//					}
					return out.toArray(new ParameterAccumulator[0]);
				}
				out.add(read);
			} catch ( IOException ex ) {
				throw new FizzleException(ex.getMessage());
			}
			first = false;
		} while ( true );


	}

	public static ParameterAccumulator parseWord(PushbackReader s, Context ctx, boolean first) throws IOException {
		return parseWord(s, ctx, first, "");
	}
	public static ParameterAccumulator parseWord(PushbackReader s, Context ctx, boolean first, String extraStop) throws IOException {
		char in = '\0';
		ParameterAccumulator current = new ParameterAccumulator();

		int r;
		while ( (r = s.read()) > 0 ) {
			char c = (char) r;
			boolean append = false;
			if ( in == '"' ) {
				if ( c == '\\' ) {
					current.append(TCLUtils.readSlashCode(s));
				} else if ( c == '"' ) {
					in = '\0';
					int xcn = s.read();
					if ( xcn > 0 ) {
						int xcnn = s.read();
						if ( xcnn > 0 ) s.unread(xcnn);
						s.unread(xcn);
						//If right after a close quote we try to eat a new line, thats okay.
						//TODO: This wount throw the correct error for something like "\\
						if ( xcn == '\\' ) xcn = xcnn;
						if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
							if ( extraStop.indexOf(xcn) == -1 ) throw new FizzleException("extra characters after close-quote");
						}
						return current;

					}
				} else if ( c == '[' ) {
					s.unread(r);
					current.append(ParameterAccumulator.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
				} else if ( c == '$' ) {
					s.unread(r);
					String name = TCLUtils.readVariableName(s, ctx);
					if ( name.length() > 0 ) {
						current.append(ParameterAccumulator.Type.VARIABLE, name, ctx);
					} else {
						append = true;
					}
				} else {
					append = true;
				}
			} else {
				if ( c == '\\' ) {
					String ta = TCLUtils.readSlashCode(s);
					if ( !ta.equals("") ) {
						current.append(ta);
						continue;
					} else {
						c = ' ';
					}
				}
				if ( !current.empty() && extraStop.indexOf(c) != -1 ) {
					s.unread(c);
					return current;
				}
				else if ( c == '"' && current.empty() ) {
					in = c;
				} else if ( c == '{' && current.empty() ) {
					s.unread(r);
					current.append(TCLUtils.readCurlyBraceStringToString(s));
					int xcn = s.read();
					if ( xcn > 0 ) {
						s.unread(xcn);
						if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
							throw new FizzleException("extra characters after close-brace");
						}
					}
				} else if ( c == '[' ) {
					s.unread(r);
					current.append(ParameterAccumulator.Type.CODE, TCLUtils.readBracketExpressionToString(s), ctx);
				} else if ( c == ' ' || c == '\t' || c == '\r' || c == (char) 11 || c == '\f' ) {
					if ( !current.empty() ) return current;
				} else if ( c == '\n' || c == ';' ) {
					if ( !current.empty() ) {
						s.unread(c);
						return current;
					} else if ( !first ) {
						return null;
					}
				} else if ( c == '$' ) {
					s.unread(r);
					String name = TCLUtils.readVariableName(s, ctx);
					if ( name.length() > 0 ) {
						current.append(ParameterAccumulator.Type.VARIABLE, name, ctx);
					} else {
						append = true;
					}
				} else if ( c == '#' && current.empty() && first ) {
					boolean before_slash = false;

					while ( c != '\n' || before_slash ) {
						r = s.read();
						if ( r < 0 ) return parseWord(s, ctx,  first);

						//Account for an escaped endline, which continues the comment
						if ( before_slash ) before_slash = false;
						else if ( c == '\\' ) before_slash = true;
						c = (char) r;
					}
					return parseWord(s, ctx, first);
				} else {
					append = true;
				}
			}

			if ( append ) current.append("" + c);

		}

		if ( in != '\0' ) throw new FizzleException("missing " + in);
		if ( !current.empty() ) return current;
		return null;
	}

	public Parameter getResult() {
		// TODO Auto-generated method stub
		if ( result == null ) return null;
		return result.getValue();
	}

	public Code getCode() {
		// TODO Auto-generated method stub
		if ( result == null ) return null;
		return result.getCode();
	}

	public EvaluationResult getDeepestEvaluationResult() {
		if ( this.sub != null ) return sub.getDeepestEvaluationResult();
		return result;
	}

}
