package com.basicer.parchment.parameters;

import java.util.Iterator;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.tcl.Set;

public class LazyParameter extends Parameter {

	private Parameter resolved;
	
	public Type type;
	public String data;
	public Context ctx;
	
	public enum Type { CODE, VARIABLE; }
	
	public LazyParameter(Parameter p) {
		resolved = p;
	}
	
	public LazyParameter(Type type, String data, Context ctx) {
		this.data = data;
		this.ctx = ctx;
		this.type = type;
	}
	
	public Parameter getResolved() {
		if ( resolved != null ) return resolved;
		EvaluationResult er = resolve();
		if ( er.getCode() != Code.OK && er.getCode() != Code.RETURN ) throw new FizzleException(er.getValue().asString());
		resolved = er.getValue();
		return resolved;
	}

	public EvaluationResult resolve() {
		if ( resolved != null ) return new EvaluationResult(resolved);
		if ( type == Type.CODE ) {
			
			EvaluationResult er = TCLEngine.cheatyEvaluate(data, ctx);
			return er;
		} else if ( type == Type.VARIABLE ) {
			try {
				return new EvaluationResult(Set.access(data, false, null, ctx));
			} catch ( FizzleException ex ) {
				return EvaluationResult.makeError(ex.getMessage());
			}
		} else {
			throw new RuntimeException("WJHASADFD?");
		}
	}
	
	@Override
	public Class getUnderlyingType() { return getResolved().getUnderlyingType(); }

	public Double asDouble(Context ctx) {
		return getResolved().asDouble(ctx);
	}

	public String asString(Context ctx) {
		return getResolved().asString(ctx);
	}

	public Integer asInteger(Context ctx) {
		return getResolved().asInteger(ctx);
	}

	public boolean asBoolean(Context ctx) {
		return getResolved().asBoolean(ctx);
	}

	public <T> T as(Class<T> type) {
		return getResolved().as(type);
	}

	public <T> T as(Class<T> type, Context ctx) {
		return getResolved().as(type, ctx);
	}

	public <T extends Parameter> T cast(Class<T> type) {
		return getResolved().cast(type);
	}

	public Parameter castByString(String name) {
		return getResolved().castByString(name);
	}

	public Parameter castByString(String name, Context ctx) {
		return getResolved().castByString(name, ctx);
	}

	public Parameter index(int n) {
		return getResolved().index(n);
	}

	public void deleteIndex(String name) {
		getResolved().deleteIndex(name);
	}

	public Parameter index(String s) {
		return getResolved().index(s);
	}

	public void writeIndex(String string, Parameter val) {
		getResolved().writeIndex(string, val);
	}

	public Object getUnderlyingValue() {
		return getResolved().getUnderlyingValue();
	}

	public <T extends Parameter> T cast(Class<T> type, Context ctx) {
		return getResolved().cast(type, ctx);
	}

	public Class<? extends Parameter> getHomogeniousType() {
		return getResolved().getHomogeniousType();
	}

	public Iterator<Parameter> iterator() {
		return getResolved().iterator();
	}

	public String toString() {
		return getResolved().toString();
	}

	public Parameter get(String string) {
		return getResolved().get(string);
	}

	public boolean equals(Object oother) {
		return getResolved().equals(oother);
	}

	public int hashCode() {
		return getResolved().hashCode();
	}

	public <T extends Enum> T asEnum(Class<T> c) {
		return getResolved().asEnum(c);
	}
	

}
