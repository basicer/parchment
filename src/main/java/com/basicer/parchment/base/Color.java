package com.basicer.parchment.base;

import com.basicer.parchment.Context;
import com.basicer.parchment.EvaluationResult;
import com.basicer.parchment.TCLCommand;
import com.basicer.parchment.TCLEngine;
import com.basicer.parchment.parameters.BlockParameter;
import com.basicer.parchment.parameters.ItemParameter;
import com.basicer.parchment.parameters.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.Material;

public class Color extends TCLCommand {

	
	@Override
	public String[] getArguments() { return new String[] { "color" }; }
	
	@Override
	public EvaluationResult extendedExecute(Context ctx, TCLEngine engine) {
		return new EvaluationResult(execute(ctx));
	}
	
	public Parameter execute(Context ctx) {
		Parameter param = ctx.get("color");
		if ( param instanceof BlockParameter ) { return Parameter.from(colorFromMaterial(((BlockParameter) param).asMaterial(ctx)).toString()); }
		if ( param instanceof ItemParameter) { return Parameter.from(colorFromMaterial(((ItemParameter) param).asItemStack(ctx).getType()).toString()); }
		String color = param.asString();
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

	private org.bukkit.ChatColor colorFromMaterial(Material type) {

		switch (type) {
			case DIAMOND:
			case DIAMOND_ORE:
			case DIAMOND_BLOCK:
			case DIAMOND_AXE:
			case DIAMOND_HOE:
			case DIAMOND_HELMET:
			case DIAMOND_SWORD:
			case DIAMOND_SPADE:
			case DIAMOND_PICKAXE:
			case DIAMOND_CHESTPLATE:
			case DIAMOND_LEGGINGS:
			case DIAMOND_BOOTS:

				return ChatColor.AQUA;
			case REDSTONE_ORE:
			case GLOWING_REDSTONE_ORE:
			case NETHER_WARTS:
			case REDSTONE_TORCH_ON:
			case REDSTONE_TORCH_OFF:
			case REDSTONE_WIRE:
			case REDSTONE:
			case REDSTONE_COMPARATOR_OFF:
			case REDSTONE_COMPARATOR_ON:

				return ChatColor.DARK_RED;
			case GOLD_INGOT:
			case GOLD_ORE:
			case PUMPKIN:
			case JACK_O_LANTERN:
			case GLOWSTONE:
			case WATCH:
			case GLOWSTONE_DUST:
			case CROPS:
			case GOLD_BLOCK:

			case GOLD_AXE:
			case GOLD_HOE:
			case GOLD_HELMET:
			case GOLD_SWORD:
			case GOLD_SPADE:
			case GOLD_PICKAXE:
			case GOLD_CHESTPLATE:
			case GOLD_LEGGINGS:
			case GOLD_BOOTS:
			case GOLDEN_APPLE:
			case GOLD_NUGGET:
			case GOLDEN_CARROT:
			case GOLD_RECORD:
				return ChatColor.GOLD;
			case MOSSY_COBBLESTONE:
			case LEAVES:
			case VINE:
			case LONG_GRASS:
			case WATER_LILY:
			case EYE_OF_ENDER:
			case SPECKLED_MELON:
				return ChatColor.DARK_GREEN;
			case IRON_ORE:
			case CLAY_BRICK:
			case CAULDRON:
			case IRON_FENCE:
			case STONE:
			case SMOOTH_BRICK:
			case COBBLESTONE:
			case COBBLESTONE_STAIRS:
			case CLAY:
			case GRAVEL:
			case DISPENSER:
			case FURNACE:
			case BURNING_FURNACE:
			case IRON_DOOR_BLOCK:
			case STONE_BUTTON:

			case IRON_AXE:
			case IRON_HOE:
			case IRON_HELMET:
			case IRON_SWORD:
			case IRON_SPADE:
			case IRON_PICKAXE:
			case IRON_CHESTPLATE:
			case IRON_LEGGINGS:
			case IRON_DOOR:
			case IRON_BOOTS:
			case IRON_BLOCK:
			case IRON_INGOT:

			case BUCKET:
			case MINECART:
				return ChatColor.GRAY;
			case LAPIS_BLOCK:
			case LAPIS_ORE:
				return ChatColor.BLUE;
			case COAL_ORE:
			case MOB_SPAWNER:
			case BROWN_MUSHROOM:
			case SOUL_SAND:

			case STONE_AXE:
			case STONE_HOE:
			case STONE_SWORD:
			case STONE_SPADE:
			case STONE_PICKAXE:
				return ChatColor.DARK_GRAY;
			case OBSIDIAN:
			case MYCEL:
			case PORTAL:

				return ChatColor.DARK_PURPLE;
			case MELON_BLOCK:
			case SUGAR_CANE_BLOCK:
			case CACTUS:
			case GRASS:
			case SAPLING:
			case EMERALD_BLOCK:
			case EMERALD:
			case EMERALD_ORE:
			case GREEN_RECORD:
				return ChatColor.GREEN;
			case BRICK:
			case BRICK_STAIRS:

			case RED_ROSE:
			case NETHERRACK:
			case RED_MUSHROOM:
			case TNT:
			case FIRE:
			case APPLE:
				return ChatColor.RED;
			case SPONGE:
			case YELLOW_FLOWER:
			case SAND:
			case SANDSTONE:
			case TORCH:
				return ChatColor.YELLOW;
			case BOW:
			case BOAT:
			case DIRT:
			case WOOD:
				return ChatColor.WHITE; //?
			case WATER:
			case STATIONARY_WATER:
				return ChatColor.DARK_BLUE;
			case SNOW:
			case WEB:
			case BONE:
			case SUGAR:
			case CAKE:
			case SNOW_BLOCK:
			case CAKE_BLOCK:
			case QUARTZ_BLOCK:
			case QUARTZ_STAIRS:

				return ChatColor.WHITE;
			case LAVA:
			case STATIONARY_LAVA:
				return ChatColor.RED;
			case BEDROCK:
			case DRAGON_EGG:
				return ChatColor.BLACK;
			case ICE:
				return ChatColor.BLUE;
			case LEATHER_HELMET:
			case LEATHER_CHESTPLATE:
			case LEATHER_LEGGINGS:
			case LEATHER_BOOTS:
				return ChatColor.YELLOW; //?
			case NETHER_BRICK:
			case NETHER_FENCE:
			case NETHER_BRICK_STAIRS:
				return ChatColor.DARK_RED;

			case LOG:
				break;
			case GLASS:
				break;
			case NOTE_BLOCK:
				break;
			case BED_BLOCK:
				break;
			case POWERED_RAIL:
				break;
			case DETECTOR_RAIL:
				break;
			case PISTON_STICKY_BASE:
				break;
			case DEAD_BUSH:
				break;
			case PISTON_BASE:
				break;
			case PISTON_EXTENSION:
				break;
			case PISTON_MOVING_PIECE:
				break;
			case DOUBLE_STEP:
				break;
			case STEP:
				break;
			case BOOKSHELF:
				break;
			case WOOD_STAIRS:
				break;
			case CHEST:
				break;
			case WORKBENCH:
				break;
			case SOIL:
				break;
			case SIGN_POST:
				break;
			case WOODEN_DOOR:
				break;
			case LADDER:
				break;
			case RAILS:
				break;
			case WALL_SIGN:
				break;
			case LEVER:
				break;
			case STONE_PLATE:
				break;
			case WOOD_PLATE:
				break;
			case JUKEBOX:
				break;
			case FENCE:
				break;
			case DIODE_BLOCK_OFF:
				break;
			case DIODE_BLOCK_ON:
				break;
			case LOCKED_CHEST:
				break;
			case TRAP_DOOR:
				break;
			case MONSTER_EGGS:
				break;
			case HUGE_MUSHROOM_1:
				break;
			case HUGE_MUSHROOM_2:
				break;
			case THIN_GLASS:
				break;
			case PUMPKIN_STEM:
				break;
			case MELON_STEM:
				break;
			case FENCE_GATE:
				break;
			case SMOOTH_STAIRS:
				break;
			case ENCHANTMENT_TABLE:
				break;
			case BREWING_STAND:
				break;
			case ENDER_PORTAL:
				break;
			case ENDER_PORTAL_FRAME:
				break;
			case ENDER_STONE:
				break;
			case REDSTONE_LAMP_OFF:
				break;
			case REDSTONE_LAMP_ON:
				break;
			case WOOD_DOUBLE_STEP:
				break;
			case WOOD_STEP:
				break;
			case COCOA:
				break;
			case SANDSTONE_STAIRS:
				break;
			case ENDER_CHEST:
				break;
			case TRIPWIRE_HOOK:
				break;
			case TRIPWIRE:
				break;
			case SPRUCE_WOOD_STAIRS:
				break;
			case BIRCH_WOOD_STAIRS:
				break;
			case JUNGLE_WOOD_STAIRS:
				break;
			case COMMAND:
				break;
			case BEACON:
				break;
			case COBBLE_WALL:
				break;
			case FLOWER_POT:
				break;
			case CARROT:
				break;
			case POTATO:
				break;
			case WOOD_BUTTON:
				break;
			case SKULL:
				break;
			case ANVIL:
				break;
			case TRAPPED_CHEST:
				break;
			case GOLD_PLATE:
				break;
			case IRON_PLATE:
				break;
			case DAYLIGHT_DETECTOR:
				break;
			case REDSTONE_BLOCK:
				break;
			case QUARTZ_ORE:
				break;
			case HOPPER:
				break;
			case ACTIVATOR_RAIL:
				break;
			case DROPPER:
				break;
			case FLINT_AND_STEEL:
				break;
			case ARROW:
				break;
			case COAL:
				break;
			case WOOD_SWORD:
				break;
			case WOOD_SPADE:
				break;
			case WOOD_PICKAXE:
				break;
			case WOOD_AXE:
				break;
			case STICK:
				break;
			case BOWL:
				break;
			case MUSHROOM_SOUP:
				break;
			case STRING:
				break;
			case FEATHER:
				break;
			case SULPHUR:
				break;
			case WOOD_HOE:
				break;
			case SEEDS:
				break;
			case WHEAT:
				break;
			case BREAD:
				break;
			case CHAINMAIL_HELMET:
				break;
			case CHAINMAIL_CHESTPLATE:
				break;
			case CHAINMAIL_LEGGINGS:
				break;
			case CHAINMAIL_BOOTS:
				break;
			case FLINT:
				break;
			case PORK:
				break;
			case GRILLED_PORK:
				break;
			case PAINTING:
				break;
			case SIGN:
				break;
			case WOOD_DOOR:
				break;
			case WATER_BUCKET:
				break;
			case LAVA_BUCKET:
				break;
			case SADDLE:
				break;
			case SNOW_BALL:
				break;
			case LEATHER:
				break;
			case MILK_BUCKET:
				break;
			case CLAY_BALL:
				break;
			case SUGAR_CANE:
				break;
			case PAPER:
				break;
			case BOOK:
				break;
			case SLIME_BALL:
				break;
			case STORAGE_MINECART:
				break;
			case POWERED_MINECART:
				break;
			case EGG:
				break;
			case COMPASS:
				break;
			case FISHING_ROD:
				break;
			case RAW_FISH:
				break;
			case COOKED_FISH:
				break;
			case INK_SACK:
				break;
			case BED:
				break;
			case DIODE:
				break;
			case COOKIE:
				break;
			case MAP:
				break;
			case SHEARS:
				break;
			case MELON:
				break;
			case PUMPKIN_SEEDS:
				break;
			case MELON_SEEDS:
				break;
			case RAW_BEEF:
				break;
			case COOKED_BEEF:
				break;
			case RAW_CHICKEN:
				break;
			case COOKED_CHICKEN:
				break;
			case ROTTEN_FLESH:
				break;
			case ENDER_PEARL:
				break;
			case BLAZE_ROD:
				break;
			case GHAST_TEAR:
				break;
			case NETHER_STALK:
				break;
			case POTION:
				break;
			case GLASS_BOTTLE:
				break;
			case SPIDER_EYE:
				break;
			case FERMENTED_SPIDER_EYE:
				break;
			case BLAZE_POWDER:
				break;
			case MAGMA_CREAM:
				break;
			case BREWING_STAND_ITEM:
				break;
			case CAULDRON_ITEM:
				break;
			case MONSTER_EGG:
				break;
			case EXP_BOTTLE:
				break;
			case FIREBALL:
				break;
			case BOOK_AND_QUILL:
				break;
			case WRITTEN_BOOK:
				break;
			case ITEM_FRAME:
				break;
			case FLOWER_POT_ITEM:
				break;
			case CARROT_ITEM:
				break;
			case POTATO_ITEM:
				break;
			case BAKED_POTATO:
				break;
			case POISONOUS_POTATO:
				break;
			case EMPTY_MAP:
				break;
			case SKULL_ITEM:
				break;
			case CARROT_STICK:
				break;
			case NETHER_STAR:
				break;
			case PUMPKIN_PIE:
				break;
			case FIREWORK:
				break;
			case FIREWORK_CHARGE:
				break;
			case ENCHANTED_BOOK:
				break;
			case REDSTONE_COMPARATOR:
				break;
			case NETHER_BRICK_ITEM:
				break;
			case QUARTZ:
				break;
			case EXPLOSIVE_MINECART:
				break;
			case HOPPER_MINECART:
				break;
			case RECORD_3:
				break;
			case RECORD_4:
				break;
			case RECORD_5:
				break;
			case RECORD_6:
				break;
			case RECORD_7:
				break;
			case RECORD_8:
				break;
			case RECORD_9:
				break;
			case RECORD_10:
				break;
			case RECORD_11:
				break;
			case RECORD_12:
				break;
			default:
				return ChatColor.WHITE;
		}
		return ChatColor.WHITE;
	}

}
