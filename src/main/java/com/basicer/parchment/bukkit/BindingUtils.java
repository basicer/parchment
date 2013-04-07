package com.basicer.parchment.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class BindingUtils {
	
	public static String getItemName(ItemStack item) {
		if ( !item.getItemMeta().hasDisplayName() ) return null;
		String name = item.getItemMeta().getDisplayName();
		if ( !name.endsWith("§]§r") ) return name;
		int beginning = name.indexOf("§[");
		return name.substring(0, beginning);
		
	}
	
	public static void setItemName(ItemStack item, String name) {
		String code = getBindingCode(item);
		if ( code != null ) name += code;
		ItemMeta m = item.getItemMeta();
		m.setDisplayName(name);
		item.setItemMeta(m);
	}
	
	public static String getDefualtItemName(Material mat) {
		String name = "";
		String[] parts = mat.toString().split("[_ ]");
		for ( int i = 0; i < parts.length; ++i ) {
			if ( i > 0 ) name += " ";
			name += Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1).toLowerCase();
		}
		return name;
	}

	public static void setBinding(ItemStack item, String binding) {
		String name = getItemName(item);
		if ( name == null ) {
			name = getDefualtItemName(item.getType());
		}
		String code = "";
		for ( int i = 0; i < binding.length(); ++i ) {
			code += "§" + binding.charAt(i);
		}
		name = name += "§[" + code + "§]§r";
		System.out.println(name);
		ItemMeta m = item.getItemMeta();
		m.setDisplayName(name);
		item.setItemMeta(m);
		
	}
	
	public static String getBinding(ItemStack item) {
		String code = getBindingCode(item);
		if ( code == null ) return null;
		String binding = "";
		for ( int i = 3; i < code.length() - 4; i += 2 ) {
			binding += code.charAt(i);
		}
		return binding;
	}
	
	public static String getBindingCode(ItemStack item) {
		String name = item.getItemMeta().getDisplayName();
		if ( name == null ) return null;
		if ( !name.endsWith("§]§r") ) return null;
		int beginning = name.indexOf("§[");
		return name.substring(beginning);
	}
	
	
}
