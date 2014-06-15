package com.basicer.parchment.parameters;

import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;

import com.basicer.parchment.Context;

import java.io.Console;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StringParameter extends Parameter {
	private String self;
	private ArrayList<ParameterAccumulator[]> cachedCode;

	public StringParameter(String str) {
		self = str;
	}

	@Override
	public Class<String> getUnderlyingType() { return String.class; }

	@Override
	public StringParameter castToStringParameter() {
		return this;
	}

	public Double asDouble(Context ctx) {
		try { 
			return Double.parseDouble(self);
		} catch (NumberFormatException ex) {
			return null;
		}
	}
	
	
	public Long asLong(Context ctx) {
		
		Number n = TCLUtils.parseStringToNumber(self);
		if ( n == null ) return null;
		try {
			return n.longValue();
		} catch (NumberFormatException ex) {
			return null;
		}
	}

	public Integer asInteger(Context ctx) {
		Long l = this.asLong(ctx);
		if ( l == null ) return null;
		return l.intValue();
	}
	
	@Override
	public String asString(Context ctx) { return self; }

	@Override
	public boolean asBoolean(Context ctx) {
		try {
			return asBooleanStrict(ctx);
		} catch ( FizzleException ex ) {
			return false;
		}
	}

	@Override
	public boolean asBooleanStrict(Context ctx) {
		if ( self.equalsIgnoreCase("true") ) return true;
		if ( self.equalsIgnoreCase("on") ) return true;
		if ( self.equalsIgnoreCase("off") ) return false;
		if ( self.equalsIgnoreCase("false") ) return false;
		if ( self.length() == 0 ) return false;
		Integer i = asInteger(ctx);
		if ( i == null ) throw new FizzleException("expected boolean value but got \"" + self + "\"");
		return ( i != 0 );
	}


	private int cacheMisses = 0;

	public List<ParameterAccumulator[]> asTCLCode(Context ctx) {
		if ( cachedCode != null ) {
			return copyTCLCode(cachedCode, ctx);
		}
		ArrayList<ParameterAccumulator[]> out = compileForTCLCode(ctx);
		if ( ++cacheMisses < 2 ) return out;
		cachedCode = copyTCLCode(out, ctx);
		return copyTCLCode(cachedCode, ctx);
	}

	private ArrayList<ParameterAccumulator[]> compileForTCLCode(Context ctx) {
		ArrayList<ParameterAccumulator[]> out = new ArrayList<ParameterAccumulator[]>();
		PushbackReader src = new PushbackReader(new StringReader(self), 2);
		ParameterAccumulator[] last = null;
		do {
			try {
				last = TCLUtils.parseLine(src, ctx, false);
			} catch ( Exception ex ) {
				last = new ParameterAccumulator[] { new ExceptionalParamaterAccmulator(ex)};
			}
			if ( last != null ) out.add(last);
		} while ( last != null );
		return out;
	}

	public static ArrayList<ParameterAccumulator[]> copyTCLCode(ArrayList<ParameterAccumulator[]> code, Context ctx) {
		ArrayList<ParameterAccumulator[]> out = new ArrayList<ParameterAccumulator[]>();
		if ( code == null ) return null;
		for ( ParameterAccumulator[] line : code ) {
			ParameterAccumulator[] copy = new ParameterAccumulator[line.length];
			for ( int i = 0; i < line.length; ++i ) {
				ParameterAccumulator src = line[i];
				if ( src.isResolved() ) copy[i] = src;
				else {
					copy[i] = src.copy();
					copy[i].writeContexts(ctx);
				}
			}
			out.add(copy);
		}

		return out;
	}

	public int hashCode() { return this.self.hashCode(); }
	public boolean equals(Object o) {
		if ( o instanceof StringParameter ) {
			return ((StringParameter)o).self.equals(self);
		} else {
			return super.equals(o);
		}
	}

	public class ExceptionalParamaterAccmulator extends ParameterAccumulator {
		public FizzleException containedException;
		public ExceptionalParamaterAccmulator(Exception ex) {
			super((Context)null);
			if ( ex instanceof  FizzleException ) containedException = (FizzleException)ex;
			else containedException = new FizzleException(ex.getMessage());
		}

		@Override
		public Parameter cheatyResolveOrFizzle() {
			throw containedException;
		}

		@Override
		public ParameterAccumulator copy() { return this; }
	}
	
}

