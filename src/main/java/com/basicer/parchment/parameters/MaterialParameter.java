package com.basicer.parchment.parameters;

import org.bukkit.Material;

import com.basicer.parchment.Context;

public class MaterialParameter extends Parameter {
	private Material self;

	MaterialParameter(Material self) {
		this.self = self;
	}
	
	@Override
	public Class<Material> getUnderlyingType() { return Material.class; }
	
	@Override
	public String asString(Context ctx) { return self.name(); }
	
	
	public Integer asInteger(Context ctx) { return self.getId(); }

	
	public Material asMaterial(Context ctx) { return self; }
	
	public static MaterialParameter castFrom(StringParameter str, Context ctx) {
		String name = str.asString();
		return new MaterialParameter(parseMaterial(name));
	}

	public static Material parseMaterial(String name) {
		org.bukkit.Material m = org.bukkit.Material.matchMaterial(name);
		if ( m != null ) return m;
		

		name = name.replace("shovel", "spade").replace("pants","leggings").replace("hat","helm").replace("helmet", "helm");
		
		m = org.bukkit.Material.matchMaterial(name);
		if ( m != null ) return m;

		name = name.replace("diamond", "d").replace("iron", "i").replace("stone", "s");
		name = name.replace("chestplate", "chest").replace("leggings", "pants");
		name = name.replace("pickaxe", "pick");
		name = name.toLowerCase().replaceAll("[._ ]*", "");
		
		if ( name.equals("dsword") ) 	return Material.DIAMOND_SWORD;
		if ( name.equals("dhoe") ) 		return Material.DIAMOND_HOE;
		if ( name.equals("dspade") ) 	return Material.DIAMOND_SPADE;
		if ( name.equals("daxe") ) 		return Material.DIAMOND_AXE;
		if ( name.equals("dpick") ) 	return Material.DIAMOND_PICKAXE;
		if ( name.equals("dpants") ) 	return Material.DIAMOND_LEGGINGS;
		if ( name.equals("dchest") ) 	return Material.DIAMOND_CHESTPLATE;
		if ( name.equals("dhelm") ) 	return Material.DIAMOND_HELMET;
		if ( name.equals("dboots") ) 	return Material.DIAMOND_BOOTS;

		if ( name.equals("gsword") ) 	return Material.GOLD_SWORD;
		if ( name.equals("ghoe") ) 		return Material.GOLD_HOE;
		if ( name.equals("gspade") ) 	return Material.GOLD_SPADE;
		if ( name.equals("gaxe") ) 		return Material.GOLD_AXE;
		if ( name.equals("gpick") ) 	return Material.GOLD_PICKAXE;
		if ( name.equals("gpants") ) 	return Material.GOLD_LEGGINGS;
		if ( name.equals("gchest") ) 	return Material.GOLD_CHESTPLATE;
		if ( name.equals("ghelm") ) 	return Material.GOLD_HELMET;
		if ( name.equals("gboots") ) 	return Material.GOLD_BOOTS;
		
		
		if ( name.equals("isword") ) 	return Material.IRON_SWORD;
		if ( name.equals("ihoe") ) 		return Material.IRON_HOE;
		if ( name.equals("ispade") ) 	return Material.IRON_SPADE;
		if ( name.equals("iaxe") ) 		return Material.IRON_AXE;
		if ( name.equals("ipick") ) 	return Material.IRON_PICKAXE;
		if ( name.equals("ipants") ) 	return Material.IRON_LEGGINGS;
		if ( name.equals("ichest") ) 	return Material.IRON_CHESTPLATE;
		if ( name.equals("ihelm") ) 	return Material.IRON_HELMET;
		if ( name.equals("iboots") ) 	return Material.IRON_BOOTS;
		
		
		if ( name.equals("ssword") ) 	return Material.STONE_SWORD;
		if ( name.equals("shoe") ) 		return Material.STONE_HOE;
		if ( name.equals("sspade") ) 	return Material.STONE_SPADE;
		if ( name.equals("sshovel") ) 	return Material.STONE_SPADE;
		if ( name.equals("saxe") ) 		return Material.STONE_AXE;
		if ( name.equals("spick") ) 	return Material.STONE_PICKAXE;
		
		
		if ( name.equals("perl") ) 		return Material.ENDER_PEARL;
		if ( name.equals("pearl") )		return Material.ENDER_PEARL;
		
		
		return null;
	}
	
	public static MaterialParameter castFrom(IntegerParameter i, Context ctx) {
			Material m = Material.getMaterial(i.asInteger().intValue());
			if ( m == null ) return null;
			return new MaterialParameter(m);
	}
	
}
