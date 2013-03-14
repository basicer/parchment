package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.Parameter;

public class Color extends TCLCommand {

	
	@Override
	public String[] getArguments() { return new String[] { "color" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		return new EvaluationResult(execute(ctx));
	}
	
	public Parameter execute(Context ctx) {
		String color = ctx.get("color").asString();
		color = color.toLowerCase();
		color = color.replace(' ', '-');
		org.bukkit.ChatColor out = null;
		
		if ( color.length() == 1 ) out = org.bukkit.ChatColor.getByChar(color);
		else if ( color.equals("black") ) out = org.bukkit.ChatColor.BLACK;
		else if ( color.equals("navy") || color.equals("dark-blue") ) out = org.bukkit.ChatColor.DARK_BLUE;
		else if ( color.equals("emrald") || color.equals("dark-green") ) out = org.bukkit.ChatColor.DARK_GREEN;
		else if ( color.equals("dark-cyan") || color.equals("dark-aqua") ) out = org.bukkit.ChatColor.DARK_AQUA;
		else if ( color.equals("blood-red") || color.equals("dark-red") ) out = org.bukkit.ChatColor.DARK_RED;
		else if ( color.equals("dark-purple") || color.equals("purple") ) out = org.bukkit.ChatColor.DARK_PURPLE;
		else if ( color.equals("gold") ) out = org.bukkit.ChatColor.GOLD;
		else if ( color.equals("grey") || color.equals("gray") ) out = org.bukkit.ChatColor.GRAY;
		else if ( color.equals("dark-grey") || color.equals("dark-gray") ) out = org.bukkit.ChatColor.DARK_GRAY;
		else if ( color.equals("indego") || color.equals("blue") ) out = org.bukkit.ChatColor.BLUE;
		else if ( color.equals("green") ) out = org.bukkit.ChatColor.GREEN;
		else if ( color.equals("cyan") || color.equals("aqua") ) out = org.bukkit.ChatColor.AQUA;
		else if ( color.equals("red") ) out = org.bukkit.ChatColor.RED;
		else if ( color.equals("light-purple") || color.equals("pink") ) out = org.bukkit.ChatColor.LIGHT_PURPLE;
		else if ( color.equals("yellow") ) out = org.bukkit.ChatColor.YELLOW;
		else if ( color.equals("white") ) out = org.bukkit.ChatColor.WHITE;
		
		else if ( color.equals("random") || color.equals("magic") )	out = org.bukkit.ChatColor.MAGIC;
		else if ( color.equals("bold") ) out = org.bukkit.ChatColor.BOLD;
		else if ( color.equals("strike") ) out = org.bukkit.ChatColor.STRIKETHROUGH;
		else if ( color.equals("underline") ) out = org.bukkit.ChatColor.UNDERLINE;
		else if ( color.equals("italics") ) out = org.bukkit.ChatColor.ITALIC;
		else if ( color.equals("reset") ) out = org.bukkit.ChatColor.RESET;
	
		if ( out != null ) return Parameter.from(out.toString());
		
		return Parameter.from("");
	}

}
