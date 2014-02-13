package com.basicer.parchment.parameters;

import java.util.ArrayList;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.EvaluationResult.Code;
import com.basicer.parchment.tcl.Set;

public class ParameterAccumulator {

	private EvaluationResult resolved;

	private StringBuilder progress;
	private int progress_step = 0;

	private ArrayList<Entry> entries;
	private TCLEngine ngn;

	private class Entry {
		public Type type;
		public String data;
		public Context ctx;

		public String toString() {
			return String.format("%s %s %s", type, data, ctx == null ? "-" : "?");
		}
	}

	public enum Type {
		STRING, CODE, VARIABLE;
	}

	public ParameterAccumulator(Parameter p) {
		resolved = EvaluationResult.makeOkay(p);
	}

	public void append(Type type, String data, Context ctx) {
		Entry e = new Entry();
		e.data = data;
		e.ctx = ctx;
		e.type = type;
		entries.add(e);
	}

	public void append(String s) {
		Entry last = null;
		if ( entries.size() > 0 ) last = entries.get(entries.size() - 1);
		if ( last == null || last.type != Type.STRING ) {
			last = new Entry();
			last.type = Type.STRING;
			last.data = s;
			entries.add(last);
			return;
		}
		last.data += s;
	}

	public ParameterAccumulator() {
		entries = new ArrayList<Entry>();
	}

    public String asString() {
        StringBuilder b = new StringBuilder();
        for ( Entry e : entries ) {
            if ( b.length() > 0 ) b.append(" ");
            if ( e.type == Type.STRING ) b.append(e.data);
            else if ( e.type == Type.VARIABLE ) { b.append("$"); b.append(e.data); }
            else return null;
        }
        return b.toString();
    }

	private EvaluationResult resolveOne(Entry e) {
		if ( e.type == Type.STRING )
			return EvaluationResult.makeOkay(Parameter.from(e.data));
		else if ( e.type == Type.VARIABLE ) {
			try {
				Parameter p = Set.access(e.data, false, null, e.ctx);
				return EvaluationResult.makeOkay(p);
			} catch (FizzleException ex) {
				return EvaluationResult.makeError(ex.getMessage());
			}

		} else {
			ngn = new TCLEngine(e.data, e.ctx);
			return null;
		}
	}

	public boolean isResolved() {
		return resolved != null;
	}

	public EvaluationResult getEvaluationResult() {
		return resolved;
	}
	
	public void resolveStep() {
		if ( resolved != null ) return;

		if ( entries.size() == 1 ) {

			if ( ngn != null ) {
				if ( !ngn.step() ) {
					resolved = ngn.getEvaluationResult();
					ngn = null;
				}
				return;
			}

			Entry e = entries.get(0);
			resolved = resolveOne(e);
			return;
		}

		if ( progress == null ) {
			progress = new StringBuilder();
			progress_step = -1;
		}

		if ( ngn != null ) {
			if ( !ngn.step() ) {
				if ( ngn.getCode() == Code.ERROR ) {
					resolved = ngn.getEvaluationResult();
					return;
				}
				Parameter s = ngn.getResult();
				progress.append(s == null ? "" : s.asString());
				ngn = null;
			}
			return;

		}

		if ( ++progress_step >= entries.size() ) {
			resolved = EvaluationResult.makeOkay(Parameter.from(progress.toString()));
			return;
		}
		EvaluationResult er = resolveOne(entries.get(progress_step));
		if ( er == null ) {
			return;
		}
		if ( er.getCode() == Code.ERROR ) resolved = er;
		progress.append(er.getValue().asString());
		
		
		

	}

	public EvaluationResult cheatyResolve() {
		while ( !isResolved() )
			resolveStep();
		return resolved;
	}

	/*
	 * public EvaluationResult resolve() { if ( resolved != null ) return new
	 * EvaluationResult(resolved);
	 * 
	 * if ( entries.size() == 1 ) { Entry e = entries.get(0); return
	 * resolveOne(e); }
	 * 
	 * StringBuilder b = new StringBuilder(); for ( Entry e : entries ) {
	 * EvaluationResult er = resolveOne(e); if ( er.getCode() == Code.ERROR )
	 * return er; b.append(er.getValue().asString()); }
	 * 
	 * return new EvaluationResult(Parameter.from(b.toString())); }
	 */

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("[");
		for ( Entry e : entries ) {
			b.append(e.type + ":" + e.data);
			b.append("|");
		}
		b.append("]");
		return b.toString();
	}

	public boolean empty() {
		return entries.size() == 0;
	}

	public Parameter cheatyResolveOrFizzle() {
		EvaluationResult er = cheatyResolve();
		if ( er.getCode() == Code.ERROR ) throw new FizzleException(er.getValue().asString());
		return er.getValue();
	}

}
