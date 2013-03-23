package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.EvaluationResult.BranchEvaluationResult;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.tclstrings.ErrorStrings;

public class TCLEngine {

	PushbackReader sourcecode;
	Context ctx;
	private EvaluationResult result = EvaluationResult.OK;
	private TCLEngine sub;
	private BranchEvaluationResult subbr;
	public boolean resiliant = false;

	public TCLEngine(String src, Context ctx) {
		sourcecode = new PushbackReader(new StringReader(src));
		this.ctx = ctx;
	}

	public TCLEngine(PushbackReader src, Context ctx) {
		sourcecode = src;
		this.ctx = ctx;
	}
	
	public TCLEngine(EvaluationResult er, Context ctx) {
		result = er;
		this.ctx = ctx;
	}

	ParameterAccumulator[] pargs = null;

	public boolean step() {
		if ( sub != null ) {
			if ( sub.step() ) return true;
			result = sub.getEvaluationResult();
			sub = null;

			if ( !(result instanceof EvaluationResult.BranchEvaluationResult) && result.getCode() == Code.ERROR ) {
				ctx.top().put("errorInfo", result.getValue());
			}

			return true;
		}

		if ( subbr != null ) {
			try {
				result = subbr.invokeCallback(result);
			} catch (FizzleException ex) {
				result = EvaluationResult.makeError(ex.getMessage());
			}
			subbr = null;
			return true;
		}

		if ( result instanceof EvaluationResult.BranchEvaluationResult ) {
			EvaluationResult.BranchEvaluationResult br = (EvaluationResult.BranchEvaluationResult) result;
			if ( br.getToRun() != null ) sub = new TCLEngine(br.getToRun(), br.getContext());
			subbr = br;
			return true;
		} else if ( result.getCode() != Code.OK ) {
			if ( result.getCode() == Code.ERROR ) {
				ctx.top().put("errorInfo", result.getValue());
			}
			if ( !resiliant ) return false;
		}

		if ( pargs != null ) {
			for ( int pi = 0; pi < pargs.length; ++pi ) {
				ParameterAccumulator r = pargs[pi];
				if ( !r.isResolved() ) {
					r.resolveStep();
					return true;
				}
				if ( r.getEvaluationResult().getCode() == Code.ERROR ) {
					result = r.getEvaluationResult();
					if ( resiliant ) {
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
			String name = rpargs[0].asString();

			TCLCommand s = ctx.getCommand(name);
			if ( s == null ) {
				pargs = null;
				result = EvaluationResult.makeError(String.format(ErrorStrings.NoCommand, name));
				return true;
			}
			try {
				Context c2 = s.bindContext(rpargs, ctx);
				result = s.extendedExecute(c2, this);
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

	public static EvaluationResult cheatyEvaluate(String toRun, Context context) {
		TCLEngine e = new TCLEngine(toRun, context);
		while ( e.step() ) {
		}
		return new EvaluationResult(e.getResult(), e.getCode());
	}

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

	public ParameterAccumulator[] parseLine(PushbackReader s, Context ctx) {
		return parseLine(s, ctx, this);
	}

	// Doesnt evaluate if there is no engine
	public static ParameterAccumulator[] parseLine(PushbackReader s, Context ctx, TCLEngine engine) {
		List<ParameterAccumulator> out = new ArrayList<ParameterAccumulator>();

		char in = '\0';
		ParameterAccumulator current = new ParameterAccumulator();
		boolean at_end = true;
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
							s.unread(xcn);

							if ( !Character.isWhitespace(xcn) && (char) xcn != ';' ) {
								System.err.println("Bad : " + xcn + " " + ((char) xcn));
								System.err.println("-> |" + current.toString() + "|");
								System.err.println("SoFar ");
								for ( ParameterAccumulator p : out )
									System.err.println(p.toString());
								throw new FizzleException("extra characters after close-quote");
							}

						}
					} else if ( c == '{' && false ) {
						s.unread(r);
						current.append(TCLUtils.readCurlyBraceStringToString(s));
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
						while ( c != '\n' ) {
							r = s.read();
							if ( r < 0 ) return null;
							c = (char) r;
						}
						return new ParameterAccumulator[0];
					} else {
						append = true;
					}
				}

				if ( append ) {
					current.append("" + c);
				}

			}

			if ( !current.empty() ) out.add(current);

		} catch (IOException e) {
			throw new Error(e);
		}

		if ( at_end && out.size() < 1 ) return null;
		return out.toArray(new ParameterAccumulator[0]);
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

}
