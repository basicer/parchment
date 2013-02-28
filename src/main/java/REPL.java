import java.io.*;

import org.bukkit.entity.Player;

import com.basicer.parchment.Context;
import com.basicer.parchment.SpellFactory;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.TCLUtils;
import com.basicer.parchment.parameters.Parameter;


public class REPL {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		Context commandctx = new Context(); 
		String line = null;
		
		SpellFactory spellfactory = new SpellFactory();
		spellfactory.loadTCLOnly();
		commandctx.setSpellFactory(spellfactory);
		StringBuilder b = new StringBuilder();
		
		Console c = System.console();
		if ( c == null ) {
			
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
	}

	public static Parameter execute(String s, Context pctx)
	{
		Context ctx = pctx.createSubContext();
		

		ctx.setSource("command");
		
		TCLEngine x = new TCLEngine(s, ctx);
		while (x.step()) { 	}
		return x.getResult();
	}
	
}
