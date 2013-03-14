package com.basicer.parchment;


import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.parameters.Parameter;

public class EvaluationResult {
	public enum Code { OK, ERROR, RETURN, BREAK, CONTINUE; }
	private Parameter value;
	private Code code = Code.OK;
	
	public static final EvaluationResult OK = new EvaluationResult();
	
	public EvaluationResult() { }
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
	
}