package com.basicer.parchment.parameters;

/**
 * Created with IntelliJ IDEA.
 * User: basicer
 * Date: 6/22/13
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpaqueParameter<T> extends Parameter {

	Class<T> type;
	T self;


	protected OpaqueParameter(T object) {
		type = (Class<T>)object.getClass();
		self = object;
	}

	@Override
	public Class<T> getUnderlyingType() {
		return type;
	}

	@Override
	public boolean isArray() {
		return false;
	}

	@Override
	public Object getUnderlyingValue() {
		return self;
	}

	public T getValue() {
		return self;
	}

	public static <U> OpaqueParameter<U> create(U object) {
		return new OpaqueParameter<U>(object);
	}
}
