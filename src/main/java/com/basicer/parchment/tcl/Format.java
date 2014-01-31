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
import com.basicer.parchment.tclstrings.ErrorStrings;


public class Format extends TCLCommand {

	@Override
	public String[] getArguments() {
		return new String[] { "formatString", "args" };
	}

	public static final Pattern format_pattern = Pattern.compile("%(?:([0-9]+)[$])?([ +#0-]{0,5}+)([1-9][0-9]*|[*])?(?:[.]([*]|[0-9]+|)){0,1}+(l|h|ll)?([^ ]|)");

	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine e) {
		String format = ctx.get("formatString").asString();

		StringBuffer result = new StringBuffer();
		Matcher m = format_pattern.matcher(format);
		ArrayList<Parameter> args = ctx.getArgs();
		int i = 0;
		boolean manual = false;
		boolean not_manual = false;

		while (m.find()) {
			// You can vary the replacement text for each match on-the-fly
			if ( m.group(6).length() == 0 ) throw new FizzleException("format string ended in middle of field specifier");
			char formatc = m.group(6).charAt(0);
			if ( formatc == '%' ) {
				m.appendReplacement(result, "%");
				continue;
			}
			
			if ( m.group(1) != null ) {
				if ( not_manual ) throw new FizzleException(ErrorStrings.FormatMixPositional);
				int want = Integer.parseInt(m.group(1));
				if ( want < 1 )  throw new FizzleException(ErrorStrings.FormatPositionOutOfRange);
				if ( want > args.size() )  throw new FizzleException(ErrorStrings.FormatPositionOutOfRange);
				i = want - 1;
				manual = true;
			} else if ( manual ) {
				throw new FizzleException(ErrorStrings.FormatMixPositional);
			} else {
				not_manual = true;
			}
			
			String padding = " ";
			boolean pad_left = true;
			boolean alternate = false;
			boolean plus_pos = false;
			boolean pos_space = false;
			
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
				case '+':
					plus_pos = true;
					break;
				case ' ':
					//pos_space = true;
					break;
				}
			}
			
			Integer width = null;
			String swidth = m.group(3);
			if ( swidth == null ) {}
			else if ( swidth.equals("*") ) {
				if ( (i+1) > args.size() ) throw new FizzleException(ErrorStrings.FormatMissingArgs);
				width = args.get(i++).asInteger();
				if ( width == null ) throw new FizzleException("expected integer but got \"" + args.get(i-1).asString() + "\"");
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
				if ( (i+1) > args.size() ) throw new FizzleException("not enough arguments for all format specifiers");
				precision = args.get(i++).asInteger();
				if ( precision == null ) throw new FizzleException("expected integer but got \"" + args.get(i-1).asString() + "\"");
			} else if ( sprecision.equals("") ) {
				precision = 0;
			} else {
				try {
					precision = Integer.valueOf(sprecision);
				} catch ( NumberFormatException ex ) {
					throw new FizzleException(ex.getMessage());
				}
			}
			

			if ( padding.equals("0") && width == null ) width = 1;

			String sf = "%" + (pad_left ? "" : "-") + (pos_space ? " " : "") + (plus_pos ? "+" : "") + (alternate ? "#" : "") + (padding != " " ? "0" : "") + (width != null ? width : "" ) + (precision != null ? "." + precision : "");
			boolean upper = false;
			
			String val = null;
			if ( (i+1) > args.size() ) throw new FizzleException(ErrorStrings.FormatMissingArgs);
			Parameter valp = args.get(i++);
			
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
				if ( formatc == 'i' ) formatc = 'd';
				Integer x = valp.asInteger();

				if ( m.group(5) != null )
				switch ( m.group(5).charAt(0) ) {
					case 'h':
						//Convert to 16bit signed integer.
						int xx = x & 0xFFFF;
						if ( (xx & 0x8000) != 0) {
							xx = (xx & 0x7FFF) - 0x8000;
						}
						x = xx;
						break;
				}

				if ( x == null ) return EvaluationResult.makeError("expected integer but got \"" + valp.asString() + "\"");
				val = String.format(sf + formatc, x);
				break;
			case 'g':
			case 'e':
			case 'f':
			case 'G':
			case 'E':
				Double d = valp.asDouble();
				if ( d == null ) return EvaluationResult.makeError("expected floating-point number but got \"" + valp.asString() + "\"");
				try {
					val = String.format(sf + formatc, d);
				} catch ( Exception ex ) {
					throw new RuntimeException("Java formatting exception " + width + " '" + sf + formatc + "' " + d + ": " + ex.toString());
				}
				if ( formatc == 'g' ) {

					while ( val.endsWith("0") && val.contains(".") ) val = val.substring(0, val.length() - 1);
					if ( val.endsWith(".") ) val = val.substring(0, val.length() - 1);
				}
				break;
			default:
				return EvaluationResult.makeError("bad field specifier \"" + formatc + "\"");
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
