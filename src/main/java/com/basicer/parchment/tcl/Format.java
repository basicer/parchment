package com.basicer.parchment.tcl;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Format extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "format", "args" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String format = ctx.get("format").asString();

		StringBuffer result = new StringBuffer();
		Matcher m = Pattern.compile("%([0-9]+[$])?([ +#-]*)([0-9]*)([a-z])").matcher(format);
		ArrayList<Parameter> args = ctx.getArgs();
		int i = 0;
		while (m.find()) {
			// You can vary the replacement text for each match on-the-fly
			char formatc = m.group(4).charAt(0);
			if ( formatc == '%' ) {
				m.appendReplacement(result, "%");
				continue;
			}
			
			String val = null;
			Parameter valp = args.get(i++);
			switch ( formatc ) {
			case 's':
				val = valp.asString();
				break;
			case 'd':
				val = valp.asInteger().toString();
				break;
				
			}
			
			m.appendReplacement(result, val);
		}
		m.appendTail(result);

		// TODO Auto-generated method stub
		return new EvaluationResult(Parameter.from(result.toString()));
	}

}
