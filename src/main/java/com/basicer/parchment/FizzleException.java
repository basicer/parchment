package com.basicer.parchment;

public class FizzleException extends RuntimeException {

	private static final long	serialVersionUID	= 4163289662961586743L;

	public FizzleException(String why) {
		super(why);
	} 

	public FizzleException() {
		super();
	} 
}


