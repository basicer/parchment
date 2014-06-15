package com.basicer.parchment;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.bukkit.ParchmentPlugin;
import com.basicer.parchment.parameters.ParameterAccumulator;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;
import com.basicer.parchment.tclstrings.ErrorStrings;
import com.google.common.base.Function;

import javax.security.auth.callback.Callback;

public class TCLEngine {

	StringParameter last_ran_code;

	PushbackReader sourcecode;
	List<ParameterAccumulator[]> tclcode;

	Context ctx;
	private EvaluationResult result = EvaluationResult.OK;
	private TCLEngine sub;
	private BranchEvaluationResult subbr;
	public boolean resilient = false;
	public Function<Callable<EvaluationResult>, EvaluationResult> commandGuard = null;

	public TCLEngine(StringParameter src, Context ctx) {
		tclcode = src.asTCLCode(ctx);
		this.ctx = ctx;
	}


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

	public ParameterAccumulator[] nextLine() throws FizzleException {
		if ( tclcode != null ) {
			if ( tclcode.isEmpty() ) return null;
			ParameterAccumulator[] result = tclcode.remove(0);
			if ( result.length < 1 ) return result;
			if ( result[0] instanceof  StringParameter.ExceptionalParamaterAccmulator ) {
				throw ((StringParameter.ExceptionalParamaterAccmulator ) result[0]).containedException;
			}
			return result;
		}
		if ( sourcecode == null ) return null;
		return TCLUtils.parseLine(sourcecode, ctx, false);
	}

	public boolean step() {
		return step(false);
	}
	
	public boolean step(boolean allow_sleeping) {
		if ( sub != null ) {
			if ( sub.step() ) return true;
			result = sub.getEvaluationResult();
			sub = null;

			if ( !(result instanceof BranchEvaluationResult) && result.getCode() == Code.ERROR ) {
				if ( result.getRefrencedCode() == null ) result.setRefrencedCode(last_ran_code);
				String rv = result.getValue() == null ? "null" : result.getValue().asString();
				String s = rv + "\n    while executing\n\"" + result.getRefrencedCode().asString() + "\"";
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

		if ( result instanceof BranchEvaluationResult ) {
			
			BranchEvaluationResult br = (BranchEvaluationResult) result;
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

		try {
			pargs = nextLine();
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
	 * EvaluationResult.com.basicer.parchment.BranchEvaluationResult) {
	 * EvaluationResult.com.basicer.parchment.BranchEvaluationResult br =
	 * (EvaluationResult.com.basicer.parchment.BranchEvaluationResult) result; result = evaluate(br);
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
