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

		String line = null;
		Context commandctx = new Context(); 
		SpellFactory spellfactory = new SpellFactory();
		spellfactory.load();
		StringBuilder b = new StringBuilder();
		while ( (line = in.readLine()) != null ) {
			b.append(line);
			b.append("\n");
		}
		
		
		Context ctx = commandctx.createSubContext();
		
		ctx.setSpellFactory(spellfactory);
		ctx.setSource("command");
		
		new TCLEngine(b.toString(), ctx).step();
	}

}
