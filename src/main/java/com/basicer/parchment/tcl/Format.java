package com.basicer.parchment.tcl;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.FizzleException;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Format extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "formatString", "args" };
	}

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String format = ctx.get("formatString").asString();

		StringBuffer result = new StringBuffer();
		Matcher m = Pattern.compile("%([0-9]+[$])?([ +#0-]*)([1-9][0-9]*|[*])?(?:[.]([0-9]*|[*]))?(l|h|ll)?([a-zA-Z%])").matcher(format);
		ArrayList<Parameter> args = ctx.getArgs();
		int i = 0;
		while (m.find()) {
			// You can vary the replacement text for each match on-the-fly
			char formatc = m.group(6).charAt(0);
			if ( formatc == '%' ) {
				m.appendReplacement(result, "%");
				continue;
			}
			
			String padding = " ";
			boolean pad_left = true;
			boolean alternate = false;
			
			String flags = m.group(2);
			for ( int f = 0; f < flags.length(); ++f ) {
				char flag = flags.charAt(f);
				switch ( flag ) {
				case '0':
					padding = "0";
					break;
				case '#':
					alternate = true;
					break;
				case '-':
					pad_left = false;
					break;
				}
			}
			
			Integer width = null;
			String swidth = m.group(3);
			if ( swidth == null ) {}
			else if ( swidth.equals("*") ) {
				width = args.get(i++).asInteger();
			} else {
				try {
					width = Integer.valueOf(swidth);
				} catch ( NumberFormatException ex ) {
					throw new FizzleException(ex.getMessage());
				}
			}
			
			Integer precision = null;
			String sprecision = m.group(4);
			if ( sprecision == null ) {}
			else if ( sprecision.equals("*") ) {
				precision = args.get(i++).asInteger();
			} else if ( sprecision.equals("") ) {
				precision = 0;
			} else {
				try {
					precision = Integer.valueOf(sprecision);
				} catch ( NumberFormatException ex ) {
					throw new FizzleException(ex.getMessage());
				}
			}
			
			String val = null;
			Parameter valp = args.get(i++);
			String sf = "%" + flags + (width != null ? width : "" ) + (precision != null ? "." + precision : "") + formatc;
			boolean upper = false;
			switch ( formatc ) {
			case 's':
				val = valp.asString();
				if ( precision != null && val.length() > precision ) val = val.substring(0, precision);
				break;
			case 'u':
				long vall = valp.asInteger();
				if ( vall < 0 ) vall = (int)vall & 0x00000000ffffffffL;
				val = "" + vall;
				break;
				/*
			case 'i':
			case 'd':
				val = valp.asInteger().toString();
				break;
			case 'x':
				val = ( alternate ? "0x" : "") + Integer.toHexString(valp.asInteger());
				break;
			case 'X':
				val = ( alternate ? "0X" : "") + Integer.toHexString(valp.asInteger()).toUpperCase();
				break;
			case 'o':
				val = Integer.toOctalString(valp.asInteger());
				if ( alternate && !val.startsWith("0") ) val = "0" + val;
				break;
			case 'b':
				val = Integer.toBinaryString(valp.asInteger());
				break;
			case 'c':
				val = "" + (char)valp.asInteger().intValue();
				break;
				*/
			case 'X':
				upper = true;
			case 'i':
			case 'd':
			case 'x':
			
			case 'o':
			case 'b':
			case 'c':
				val = String.format(sf, valp.asInteger());
				break;
			case 'g':
			case 'e':
			case 'f':
			case 'G':
			case 'E':
				val = String.format(sf, valp.asDouble());
				break;
			default:
				val = "[Unknown Format: " + formatc + "]";
			}
			
			if ( upper ) val = val.toUpperCase();
			
			if ( formatc == 'o' && alternate && val.equals("00") ) val = "0"; //Fix inconstancy .
			
			if ( width != null ) {
				while ( val.length() < width ) {
					if ( pad_left ) val = padding + val;
					else val += padding;
				}
			}
			
			m.appendReplacement(result, val);
		}
		m.appendTail(result);

		// TODO Auto-generated method stub
		return new EvaluationResult(Parameter.from(result.toString()));
	}

}
