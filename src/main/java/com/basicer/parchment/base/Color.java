package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.parameters.Parameter;

public class Color extends TCLCommand {

	
	@Override
	public String[] getArguments() { return new String[] { "color" }; }
	
	@Override
	public Parameter execute(Context ctx) {
		String code = "§";
		String color = ctx.get("color").asString();
		
		if ( color.equals("0") || color.equals("black") ) 		return Parameter.from(code + "0");
		if ( color.equals("1") || color.equals("dark-blue") ) 	return Parameter.from(code + "1");
		if ( color.equals("2") || color.equals("dark-green") ) 	return Parameter.from(code + "2");
		if ( color.equals("3") || color.equals("dark-aqua") ) 	return Parameter.from(code + "3");
		if ( color.equals("4") || color.equals("dark-red") ) 	return Parameter.from(code + "4");
		if ( color.equals("5") || color.equals("purple") ) 		return Parameter.from(code + "5");
		if ( color.equals("6") || color.equals("gold") ) 		return Parameter.from(code + "6");
		if ( color.equals("7") || color.equals("grey") ) 		return Parameter.from(code + "7");
		if ( color.equals("8") || color.equals("dark-grey") ) 	return Parameter.from(code + "8");
		if ( color.equals("9") || color.equals("indigo") ) 		return Parameter.from(code + "9");
		if ( color.equals("a") || color.equals("green") ) 		return Parameter.from(code + "a");
		if ( color.equals("b") || color.equals("aqua") ) 		return Parameter.from(code + "b");
		if ( color.equals("c") || color.equals("red") ) 		return Parameter.from(code + "c");
		if ( color.equals("d") || color.equals("pink") ) 		return Parameter.from(code + "d");
		if ( color.equals("e") || color.equals("yellow") ) 		return Parameter.from(code + "e");
		if ( color.equals("f") || color.equals("white") ) 		return Parameter.from(code + "f");
		
		if ( color.equals("k") || color.equals("random") ) 		return Parameter.from(code + "k");
		if ( color.equals("k") || color.equals("bold") ) 		return Parameter.from(code + "l");
		if ( color.equals("k") || color.equals("strike") ) 		return Parameter.from(code + "m");
		if ( color.equals("k") || color.equals("underline") )	return Parameter.from(code + "n");
		if ( color.equals("k") || color.equals("italics") ) 	return Parameter.from(code + "o");
		if ( color.equals("k") || color.equals("reset") ) 		return Parameter.from(code + "r");
	
		return Parameter.from("");
	}

}
