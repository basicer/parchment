package com.basicer.parchment.bukkit;

import java.io.File;

public class FSUtils {

	public static File findFile(File folder, String file) {
		if (folder == null)
			return null;
		File rfile = null;
		for (File f : folder.listFiles()) {
			if (f.isDirectory())
				continue;
			if (!f.canRead())
				continue;
			if (f.getName().equals(file)) {
				rfile = f;
				break;
			}
		}
		return rfile;
	}

	public static File findOrCreateDirectory(File folder, String file) {
		File rfile = new File(folder, file);
		if ( !rfile.exists() ) rfile.mkdirs();
		return rfile;
	}

	
}
