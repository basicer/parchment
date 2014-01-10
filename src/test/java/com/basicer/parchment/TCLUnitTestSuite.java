package com.basicer.parchment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.parameters.DictionaryParameter;
import com.basicer.parchment.parameters.Parameter;
import com.basicer.parchment.tcl.StringCmd;
import com.basicer.parchment.test.Test;
import com.basicer.parchment.test.Test.TestResult;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TCLUnitTestSuite extends TestCase {

	TestResult	result;

	public String getName() {
		return this.getClass().getName() + " : " + result.name + " - " + result.description;
	}

	public TCLUnitTestSuite(TestResult tr) {
		super("testMethod");
		this.result = tr;
	}

	// Here's the actual test
	public void testMethod() {
		
		
		assertNotNull(result);
		if ( result.expectedCode != result.resultCode ) {
			assertTrue(result.why, false);
		} else {
			assertNotNull(result);
			if ( result.result == null ) {
				assertNull(result.why, result.why);
			}

			if ( result.match == null || result.match.equals("exact")) {

				assertEquals(result.expected.asString(), result.result.asString());
			} else if ( result.match.equals("glob") ) {
				if ( !result.expected.asString().equals("*") ) {
					assertTrue(
							"\"" + result.expected.asString() + "\" doesn't match \"" + result.result.asString() + "\"",
							StringCmd.GlobMatch(result.result.asString(), result.expected.asString())
					);
				}
			} else {
				assertTrue("Unknown match type " + result.match, false);
			}
			
			assertNull(result.why, result.why);
		}
	}

	public static List<String> getListOfFiles() {
		List<String> out = new ArrayList<String>();
		//out.add("error.tcl");  //Needs the try command those error globals.
	
		//out.add("append.tcl");
		
		//out.add("if-old.tcl");
		out.add("format.tcl");
		
		out.add("parseOld.tcl");
		out.add("set.tcl");
		out.add("incr.tcl");
		out.add("concat.tcl");
		out.add("list.tcl");
		out.add("eval.tcl");
		return out;
	}

	// Here's the magic
	public static TestSuite suite() {

		TestSuite s = new TestSuite();

		for (String filename : getListOfFiles()) {
			String sep = File.separator;
			File f = new File("src" + sep + "test" + sep + "tcl" + sep + filename);
			if (!f.exists()) continue;

			FileReader fr;
			try {
				fr = new FileReader(f);

				PushbackReader pbr = new PushbackReader(fr);

				Context commandctx = new Context();

				DictionaryParameter platform = new DictionaryParameter();
				platform.writeIndex("os", Parameter.from(System.getProperty("os.name")));
				platform.writeIndex("osVersion", Parameter.from(System.getProperty("os.version")));

				commandctx.put("tcl_platform", platform);


				SpellFactory spellfactory = new SpellFactory();
				spellfactory.loadTCLOnly();
				commandctx.setSpellFactory(spellfactory);

				TCLEngine x = new TCLEngine(pbr, commandctx);
				x.resilient = true;
				while (x.step()) {
				}
				//if ( x.getCode() == Code.ERROR ) throw new RuntimeException(filename + " : " + x.getResult());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		for (TestResult r : Test.tests) {
			s.addTest(new TCLUnitTestSuite(r));
		}

		return s;
	}
}
