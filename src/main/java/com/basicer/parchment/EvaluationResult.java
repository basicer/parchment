package com.basicer.parchment;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class EvaluationResult {
	public enum Code { OK, ERROR, RETURN, BREAK, CONTINUE; }
	private Parameter value;
	private Code code = Code.OK;

	public String getRefrencedCode() {
		return refrencedCode;
	}

	public void setRefrencedCode(String refrencedCode) {
		this.refrencedCode = refrencedCode;
	}

	private String refrencedCode;

	public static final EvaluationResult OK = new EvaluationResult();
	
	public EvaluationResult() {
		this.value = Parameter.EmptyString;
		this.code = Code.OK;		
	}
	public EvaluationResult(Parameter value) {
		this.value = value;
		this.code = Code.OK;
	}
	
	public EvaluationResult(Parameter value, Code code) {
		this.value = value;
		this.code = code;
	}
	
	public Parameter getValue() { return value; }
	public Code getCode() { return code; }
	public void setCode(Code code) {
		this.code = code;
	}
	public static EvaluationResult makeError(String string) {
		return new EvaluationResult(Parameter.from(string), Code.ERROR);
	}
	
	
	public static interface EvalCallback {
		public EvaluationResult result(EvaluationResult last);
	}
	
	public static class BranchEvaluationResult extends EvaluationResult {


		
		private EvalCallback callback;
		private String toRun;
		private Context context;
		private Callable<Long> scheduleAfter; 
		
		public BranchEvaluationResult(String toRun, Context ctx, EvalCallback evalCallback) {
			this.toRun = toRun;
			this.callback = evalCallback;
			this.context = ctx;
		}
		
		public BranchEvaluationResult(String toRun, Context ctx, EvalCallback evalCallback, Callable<Long> when ) {
			this(toRun, ctx, evalCallback);
			scheduleAfter = when;
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
		
		public String getToRun() {
			return toRun;
		}
		
		public Context getContext() {
			return context;
		}
	}
	
	public String toString() {
		return "[ER: " + code.toString() + " " + (value != null ? value.toString() : "NULL") + "]";
	}
}




