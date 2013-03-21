package com.basicer.parchment;


import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class EvaluationResult {
	public enum Code { OK, ERROR, RETURN, BREAK, CONTINUE; }
	private Parameter value;
	private Code code = Code.OK;
	
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
		
		public BranchEvaluationResult(String toRun, Context ctx, EvalCallback callback) {
			this.toRun = toRun;
			this.callback = callback;
			this.context = ctx;
		}
		
		@Override
		public Parameter getValue() {
			throw new RuntimeException("You've dome something wrong");
		}

		@Override
		public Code getCode() {
			throw new RuntimeException("You've dome something wrong");
		}

		@Override
		public void setCode(Code code) {
			throw new RuntimeException("You've dome something wrong");
		}
		
		public EvaluationResult invokeCallback(EvaluationResult data) {
			return callback.result(data);
		}
		
		public String getToRun() {
			return toRun;
		}
		
		public Context getContext() {
			return context;
		}
	}
	
}




