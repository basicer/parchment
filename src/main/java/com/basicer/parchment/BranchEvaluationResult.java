package com.basicer.parchment;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.parameters.StringParameter;

import java.util.concurrent.Callable;

/**
 * Created by basicer on 2/9/14.
 */


public class BranchEvaluationResult extends EvaluationResult {



	private EvalCallback callback;
	private StringParameter toRun;
	private Context context;
	private Callable<Long> scheduleAfter;

	public BranchEvaluationResult(StringParameter toRun, Context ctx, EvalCallback evalCallback) {
		this.toRun = toRun;
		this.callback = evalCallback;
		this.context = ctx;
	}

	public BranchEvaluationResult(StringParameter toRun, Context ctx, EvalCallback evalCallback, Callable<Long> when ) {
		this(toRun, ctx, evalCallback);
		scheduleAfter = when;
	}

	public BranchEvaluationResult(StringParameter toRun, Context ctx, EvalCallback evalCallback, final long delay ) {
		this(toRun, ctx, evalCallback);
		scheduleAfter = new Callable<Long>() {
			@Override
			public Long call() throws Exception {
				return System.currentTimeMillis() + delay;
			}
		};
	}

	@Override
	public Parameter getValue() {
		throw new RuntimeException("You've done something wrong");
	}

	@Override
	public Code getCode() {
		throw new RuntimeException("You've done something wrong");
	}

	public Long getScheduleAfter() {
		if ( scheduleAfter != null ) try {
			return scheduleAfter.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public void setCode(Code code) {
		throw new RuntimeException("You've dome something wrong");
	}

	public EvaluationResult invokeCallback(EvaluationResult data) {
		if ( callback != null ) return callback.result(data);
		return EvaluationResult.OK;
	}

	public StringParameter getToRun() {
		return toRun;
	}

	public Context getContext() {
		return context;
	}
}