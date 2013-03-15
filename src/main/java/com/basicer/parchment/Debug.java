package com.basicer.parchment;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Debug {
	
	public static boolean ShowTrace = true;
	
	public static void trace(String format, Object... args) {
		String str = String.format(format, args);
		Logger l = Logger.getLogger("Parchment");
		if ( ShowTrace ) 
			l.info("{T} " + str);
		else 
			l.finer(str);
	}

	public static void info(String format, Object... args) {
		String str = String.format(format, args);
		Logger l = Logger.getLogger("Parchment");
		l.info(str);
	}
}
