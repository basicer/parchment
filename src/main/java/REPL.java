import java.io.*;
import java.util.logging.*;

import org.bukkit.entity.Player;

import com.basicer.parchment.Context;
import com.basicer.parchment.SpellFactory;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.test.Test;


public class REPL {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		Context commandctx = new Context(); 
		
		DictionaryParameter platform = new DictionaryParameter();
		platform.writeIndex("os", Parameter.from(System.getProperty("os.name")));
		platform.writeIndex("osVersion", Parameter.from(System.getProperty("os.version")));
		
		commandctx.put("tcl_platform", platform);
		commandctx.put("rob", Parameter.from("cool"));
		String line = null;
		
		SpellFactory spellfactory = new SpellFactory();
		spellfactory.loadTCLOnly();
		commandctx.setSpellFactory(spellfactory);
		StringBuilder b = new StringBuilder();
		
		Console c = System.console();
		if ( c == null ) {
			System.out.println();
			while ( (line = in.readLine()) != null ) {
				b.append(line);
				b.append("\n");
			}
			
			execute(b.toString(), commandctx);
		} else {
			
			while ( (line = c.readLine("TCL> ")) != null ) {
				c.printf("-> %s\n", execute(line, commandctx));
			}
		}
		
		//System.out.println("\n\nTotal " + Test.tests + " , " + Test.passed + " passed");
	}

	public static Parameter execute(String s, Context pctx)
	{
		//Context ctx = pctx.createSubContext();
		

		//ctx.setSource("command");
		
		TCLEngine x = new TCLEngine(s, pctx);
		while (x.step()) { 	}
		return x.getResult();
	}
	
}
