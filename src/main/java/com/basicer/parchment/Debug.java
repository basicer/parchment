package com.basicer.parchment;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Debug {

	public static boolean ShowInfo = false;
	public static boolean ShowTrace = false;
	
	public static void trace(String format, Object... args) {
		if ( !ShowTrace ) return;
		String str = String.format(format, args);
		Logger.getLogger("Parchment").info("{T} " + str);
	}

	public static void info(String format, Object... args) {
		if ( !ShowInfo ) return;
		String str = String.format(format, args);
		Logger l = Logger.getLogger("Parchment");
		l.info(str);
	}
}
