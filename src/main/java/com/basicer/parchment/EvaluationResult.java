package com.basicer.parchment;


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
	
	EvaluationResult() {
		this.value = Parameter.EmptyString;
		this.code = Code.OK;		
	}

	public static EvaluationResult makeOkay(Parameter value) {
		return new EvaluationResult(value, Code.OK);
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

	public static EvaluationResult makeError(Parameter val) {
		return new EvaluationResult(val, Code.ERROR);
	}


	public static interface EvalCallback {
		public EvaluationResult result(EvaluationResult last);
	}
	

	
	public String toString() {
		return "[ER: " + code.toString() + " " + (value != null ? value.toString() : "NULL") + "]";
	}
}




