import java.io.*;
import java.util.List;

import com.basicer.parchment.*;
import com.basicer.parchment.parameters.DictionaryParameter;

import com.basicer.parchment.EvaluationResult.Code;
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

		DictionaryParameter platform = new DictionaryParameter();
		platform.writeIndex("os", Parameter.from(System.getProperty("os.name")));
		platform.writeIndex("osVersion", Parameter.from(System.getProperty("os.version")));

		commandctx.put("tcl_platform", platform);
		commandctx.put("rob", Parameter.from("cool"));
		String line = null;

		CommandFactory spellfactory = new CommandFactory();
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
			c.printf("-> %s\n", execute(b.toString(), commandctx));
		} else {
			StringBuilder buffer = new StringBuilder();
			while ( (line = c.readLine(buffer.length() == 0 ? "TCL> " : "---> ")) != null ) {
				buffer.append(line);
				buffer.append("\n");
				String test = buffer.toString();
				for (String tc : TCLUtils.tabComplete(test, commandctx) ) c.printf("C %s\n", tc);

				if ( TCLUtils.isCompleteStatement(test) ) {
					buffer = new StringBuilder();

					c.printf("-> %s\n", execute(test, commandctx));
				}
			}
		}

		if ( com.basicer.parchment.test.Test.tests != null )
			for ( com.basicer.parchment.test.Test.TestResult r : com.basicer.parchment.test.Test.tests) {
				boolean ok = r.why == null;
				System.out.println(String.format("%s %s %s - %s", ok ? "+" : "-", r.name, r.description, r.why == null ? "Success" : r.why));
			}
		//System.out.println("\n\nTotal " + Test.tests + " , " + Test.passed + " passed");
	}

	public static Parameter execute(String s, Context pctx)
	{
		//Context ctx = pctx.createSubContext();


		//ctx.setSource("command");

		TCLEngine x = new TCLEngine(s, pctx);
		while (x.step()) { Debug.trace("STEP");	}
		if (x.getCode() != Code.OK ) {
			System.out.println("|" + x.getCode() + "| " + x.getResult());
		}
		return x.getResult();
	}

}
