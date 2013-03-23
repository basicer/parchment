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
		org.bukkit.Material m = org.bukkit.Material.matchMaterial(name);
		if ( m != null ) return new MaterialParameter(m);
		

		name = name.replace("shovel", "spade").replace("pants","leggings").replace("hat","helmet");
		
		m = org.bukkit.Material.matchMaterial(name);
		if ( m != null ) return new MaterialParameter(m);

		name = name.replace("diamond", "d").replace("iron", "i").replace("stone", "s");
		name = name.replace("chestplate", "chest").replace("leggings", "pants");
		name = name.replace("pickaxe", "pick");
		name = name.toLowerCase().replaceAll("[._ ]*", "");
		
		if ( name.equals("dsword") ) 	return new MaterialParameter(Material.DIAMOND_SWORD);
		if ( name.equals("dhoe") ) 		return new MaterialParameter(Material.DIAMOND_HOE);
		if ( name.equals("dspade") ) 	return new MaterialParameter(Material.DIAMOND_SPADE);
		if ( name.equals("daxe") ) 		return new MaterialParameter(Material.DIAMOND_AXE);
		if ( name.equals("dpick") ) 	return new MaterialParameter(Material.DIAMOND_PICKAXE);
		if ( name.equals("dpants") ) 	return new MaterialParameter(Material.DIAMOND_LEGGINGS);
		if ( name.equals("dchest") ) 	return new MaterialParameter(Material.DIAMOND_CHESTPLATE);
		if ( name.equals("dhelmet") ) 	return new MaterialParameter(Material.DIAMOND_HELMET);
		if ( name.equals("dboots") ) 	return new MaterialParameter(Material.DIAMOND_BOOTS);

		if ( name.equals("gsword") ) 	return new MaterialParameter(Material.GOLD_SWORD);
		if ( name.equals("ghoe") ) 		return new MaterialParameter(Material.GOLD_HOE);
		if ( name.equals("gspade") ) 	return new MaterialParameter(Material.GOLD_SPADE);
		if ( name.equals("gaxe") ) 		return new MaterialParameter(Material.GOLD_AXE);
		if ( name.equals("gpick") ) 	return new MaterialParameter(Material.GOLD_PICKAXE);
		if ( name.equals("gpants") ) 	return new MaterialParameter(Material.GOLD_LEGGINGS);
		if ( name.equals("gchest") ) 	return new MaterialParameter(Material.GOLD_CHESTPLATE);
		if ( name.equals("ghelmet") ) 	return new MaterialParameter(Material.GOLD_HELMET);
		if ( name.equals("gboots") ) 	return new MaterialParameter(Material.GOLD_BOOTS);
		
		
		if ( name.equals("isword") ) 	return new MaterialParameter(Material.IRON_SWORD);
		if ( name.equals("ihoe") ) 		return new MaterialParameter(Material.IRON_HOE);
		if ( name.equals("ispade") ) 	return new MaterialParameter(Material.IRON_SPADE);
		if ( name.equals("iaxe") ) 		return new MaterialParameter(Material.IRON_AXE);
		if ( name.equals("ipick") ) 	return new MaterialParameter(Material.IRON_PICKAXE);
		if ( name.equals("ipants") ) 	return new MaterialParameter(Material.IRON_LEGGINGS);
		if ( name.equals("ichest") ) 	return new MaterialParameter(Material.IRON_CHESTPLATE);
		if ( name.equals("ihelmet") ) 	return new MaterialParameter(Material.IRON_HELMET);
		if ( name.equals("iboots") ) 	return new MaterialParameter(Material.IRON_BOOTS);
		
		
		if ( name.equals("ssword") ) 	return new MaterialParameter(Material.STONE_SWORD);
		if ( name.equals("shoe") ) 		return new MaterialParameter(Material.STONE_HOE);
		if ( name.equals("sspade") ) 	return new MaterialParameter(Material.STONE_SPADE);
		if ( name.equals("saxe") ) 		return new MaterialParameter(Material.STONE_AXE);
		if ( name.equals("spick") ) 	return new MaterialParameter(Material.STONE_PICKAXE);
		
		
		if ( name.equals("perl") ) 		return new MaterialParameter(Material.ENDER_PEARL);
		
		
		return null;
	}
	
	public static MaterialParameter castFrom(IntegerParameter i, Context ctx) {
			Material m = Material.getMaterial(i.asInteger().intValue());
			if ( m == null ) return null;
			return new MaterialParameter(m);
	}
	
}
