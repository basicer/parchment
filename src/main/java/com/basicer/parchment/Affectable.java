package com.basicer.parchment;

import com.basicer.parchment.parameters.Parameter;


public interface Affectable<T> {
	public Parameter affect(T target, Context ctx);
}

