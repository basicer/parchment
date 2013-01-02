package com.basicer.parchment.unsafe;

public interface ParchmentNBTTagCompound extends ParchmentNBTBase {

	public abstract boolean getBoolean(String s);
	public abstract byte getByte(String arg0);
	public abstract byte[] getByteArray(String arg0);
	public abstract double getDouble(String arg0);
	public abstract float getFloat(String arg0);
	public abstract int getInt(String arg0);
	public abstract int[] getIntArray(String arg0);
	public abstract long getLong(String arg0);
	

	public abstract short getShort(String arg0);
	public abstract String getString(String arg0);
	public abstract byte getTypeId();
	public abstract boolean hasKey(String s);
	
	public abstract void setBoolean(String s, boolean flag);
	public abstract void setByte(String s, byte b0);
	public abstract void setByteArray(String s, byte[] abyte);
	public abstract void setDouble(String s, double d0);
	public abstract void setFloat(String s, float f);
	public abstract void setInt(String s, int i);
	public abstract void setIntArray(String s, int[] aint);
	public abstract void setLong(String s, long i);
	public abstract void setShort(String s, short short1);
	public abstract void setString(String s, String s1);
	
	public abstract ParchmentNBTBase get(String s);
	public abstract ParchmentNBTTagCompound getCompound(String s);
	public abstract ParchmentNBTTagList getList(String s);

}