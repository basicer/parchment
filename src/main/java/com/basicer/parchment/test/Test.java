package com.basicer.parchment.test;

import java.util.ArrayList;
import java.util.List;

import com.basicer.parchment.tcl.StringCmd;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Test extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "name", "description", "args" };
	}

	public static class TestResult {
		public String		name;
		public String		description;
		public String		setup;
		public String		body;
		public String		match;
		public Parameter	expected;
		public int			expectedCode;

		public Parameter	result;
		public int			resultCode;

		public String		why;
	}

	public static List<TestResult>	tests;

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {

		TestResult test = new TestResult();
		Parameter constraint = null;

		test.name = ctx.get("name").asString();
		test.description = ctx.get("description").asString();
		test.body = null;
		test.setup = null;
		test.expected = null;
		test.expectedCode = -1;

		ArrayList<Parameter> args = ctx.getArgs();
		if (args.size() < 1) return EvaluationResult.makeError("No arguments to test.");
		int count = args.size();
		if (!args.get(0).asString().startsWith("-")) {
			// Old style
			for (int i = 0; i < count; ++i) {
				int backward = count - i - 1;
				if (i == 0)
					test.expected = args.get(backward);
				else if (i == 1)
					test.body = args.get(backward).asString();
				else {
					constraint = args.get(i - 2);
				}
			}
		} else {
			if (count % 2 == 1) return EvaluationResult.makeError("I dont want to deal with this right now.");
			for (int i = 0; i < count; i += 2) {
				String action = args.get(i).asString();
				if (!action.startsWith("-")) return EvaluationResult.makeError("All test options start with -");
				Parameter value = args.get(i + 1);

				if (action.equals("-body"))
					test.body = value.asString();
				if (action.equals("-setup"))
					test.setup = value.asString();
				else if (action.equals("-result"))
					test.expected = value;
				else if (action.equals("-match"))
					test.match = value.asString();
				else if (action.equals("-constraints"))
					constraint = value;
				else if (action.equals("-returnCodes")) {
					if (value.asInteger() != null)
						test.expectedCode = value.asEnum(EvaluationResult.Code.class).ordinal();
					else
						test.expectedCode = EvaluationResult.Code.valueOf(value.asString().toUpperCase()).ordinal();
				}
			}
		}
		if (test.body == null) test.why = "All tests need a body.";
		else if (test.body == null) test.why = "All tests need a result.";
		else if (test.expected.asString() == null) test.why = "Result wasn't a string.";

		if ( constraint != null ) {
				String cs = constraint.asString();
			test.name += " (" + cs + ")";
				if ( !TestConstraint.getConstraint(cs) ) {
					return EvaluationResult.OK;
				}
		}

		EvaluationResult testResult = null;
		try {
			//Context ctxx = ctx.up(1).mergeAndCopyAsGlobal();
			Context ctxx = ctx.up(1);

			long time = System.currentTimeMillis();
			if ( test.setup != null ) {
				TCLEngine ngnsetup = new TCLEngine(test.setup, ctxx);

				while (ngnsetup.step()) {
					if ( System.currentTimeMillis() - time > 1000 ) {
						testResult = EvaluationResult.makeError("Test time limit reached....");
						break;
					}
				}
			}

			TCLEngine ngn = new TCLEngine(test.body, ctxx);
			while (ngn.step()) {
				if ( System.currentTimeMillis() - time > 1000 ) {
					testResult = EvaluationResult.makeError("Test time limit reached....");
					break;
				}
			}

			testResult = new EvaluationResult(ngn.getResult(), ngn.getCode());

		} catch (Throwable ex) {
			test.why = "Exception: " + ex.getMessage() + "\n";
			for ( StackTraceElement se : ex.getStackTrace() ) {
				test.why += se.toString() + "\n";
			}
		}

		if (testResult == null) {
			if ( test.why == null ) test.why = ".evaluate was null?!";
		} else {
			test.result = testResult.getValue();
			if (testResult.getCode() == null) {
				test.why = "Null errorcode?!";
			} else {
				test.resultCode = testResult.getCode().ordinal();
			}

			if (test.expectedCode == -1 && test.resultCode != 0 && test.resultCode != 2) { //OK or RETURN
				String value = (testResult.getValue() == null) ? "null" : testResult.getValue().asString();
				test.why = String.format("Expected good return code of got %d (%s)",
						testResult.getCode().ordinal(),
						value
				);

			} else if (test.expectedCode != -1 && test.resultCode != test.expectedCode) {
				String value = (testResult.getValue() == null) ? "null" : testResult.getValue().asString();
				test.why = String.format("Expected return code of %d got %d (%s)",
						test.expectedCode,
						testResult.getCode().ordinal(),
						value
				);
			} else if (test.result == null) {
				test.why = String.format("Expected some return value (%s) but got a real null.",
						test.expected.asString());
			} if ( test.match != null ) {
				if ( test.result != null && test.expected != null ) {
					if ( test.match.equals("exact")) {
						if ( !( test.expected.asString().equals(test.result.asString()) ) ) {
							test.why = String.format("Expected '%s' got '%s'", test.expected.toString(), test.result.toString());
						}
					} else if ( test.match.equals("glob") ) {
						if ( !test.expected.asString().equals("*") ) {
							if ( !StringCmd.GlobMatch(test.result.asString(), test.expected.asString()) ) {
								test.why = "\"" + test.expected.asString() + "\" doesn't match \"" + test.result.asString() + "\"";
							}
						}
					} else {
						test.why = "Unknown match type";
					}
				} else {
					test.why = "Null return from engine during excution";
				}
			}
		}

		/*
		 * if ( test.why == null ) { System.out.println(" + " + test.name + " "
		 * + test.description + " - Passed " + " \n"); } else {
		 * System.out.println(" - " + test.name + " " + test.description + " - "
		 * + test.why + " \n"); }
		 */

		if (tests == null) tests = new ArrayList<TestResult>();
		tests.add(test);

		return EvaluationResult.OK;
	}

}
