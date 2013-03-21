package com.basicer.parchment.test;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.fusesource.jansi.Ansi;

import com.basicer.parchment.Context;
import com.basicer.parchment.Debug;
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
		public String		body;
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

		test.name = ctx.get("name").asString();
		test.description = ctx.get("description").asString();
		test.body = null;
		test.expected = null;
		test.expectedCode = 0;

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
					Parameter constraint = args.get(i - 2);
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
				else if (action.equals("-result"))
					test.expected = value;
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
		else if (test.expected.asString() == null) test.why = "Result wasent a string.";

		EvaluationResult testResult = null;
		try {
			Context ctxx = ctx.up(1).mergeAndCopyAsGlobal();
			TCLEngine ngn = new TCLEngine(test.body, ctxx);
			while (ngn.step()) {
			}
			;

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

			if (test.resultCode != test.expectedCode) {
				test.why = String.format("Expcted return code of %d got %d (%s)", test.expectedCode, testResult
						.getCode().ordinal(), testResult.getValue().asString());
			} else if (test.result == null) {
				test.why = String.format("Expected some return value (%s) but got a real null.",
						test.expected.asString());
			} else if (!(test.result.asString().equals(test.expected.asString()))) {
				test.why = String.format("Expcted |%s| got |%s|", test.expected.asString(), testResult.getValue()
						.asString());
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
