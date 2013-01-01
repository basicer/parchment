package com.basicer.parchment.craftbukkit;



import java.lang.reflect.Field;

import org.bukkit.Material;

import org.bukkit.craftbukkit.v1_4_6.inventory.*;

import net.minecraft.server.v1_4_6.*;


/**
 *
 * @author basicer
 */
public class Book {
   
    private ItemStack handle;
    private org.bukkit.inventory.ItemStack base;
    
    public static String readSpell(org.bukkit.inventory.ItemStack itm) {
    	if ( itm == null ) return null;
    	
    	NBTTagCompound tag = getTag(itm, false);
    	if ( tag == null ) return null;
    	return tag.getString("binding");
    }
    
    private static NBTTagCompound getTag(org.bukkit.inventory.ItemStack cis, boolean set_if_empty) {
    	Field fhandle;
		try {
			fhandle = CraftItemStack.class.getDeclaredField("handle");
	    	fhandle.setAccessible(true);
	    	Object o = fhandle.get(cis);
	    	net.minecraft.server.v1_4_6.ItemStack nis = (net.minecraft.server.v1_4_6.ItemStack)o;
	    	if ( nis.getTag() == null && set_if_empty ) nis.setTag(new NBTTagCompound());
	    	return nis.getTag();
	    	
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    
    public static void setSpell(org.bukkit.inventory.ItemStack cis, String value) {
    	if ( cis == null ) return;
    	
    	getTag(cis,true).setString("binding", value);
	    	
    }
    
    public static Book createFromBukkitItemStack(org.bukkit.inventory.ItemStack base) {
         return new Book(base);
    }
    
    private Book(org.bukkit.inventory.ItemStack base) {
        if ( base.getType() != Material.BOOK_AND_QUILL && base.getType() != Material.WRITTEN_BOOK ) {
            throw new java.lang.IllegalArgumentException("Item is not a book.");
        }
        
        if ( !(base instanceof CraftItemStack) ) {
            throw new java.lang.IllegalArgumentException("Not using CraftBucket as server.");
        }
        
        this.handle = CraftItemStack.asNMSCopy(base);
        this.base = base;
    }
    
    public String getFullText() {
        NBTTagCompound tag = handle.getTag();
        if ( tag == null ) return null;
        NBTTagList list = tag.getList("pages");
        if ( list == null ) return null;
        
        StringBuilder b = new StringBuilder();
        for ( int i = 0; i < list.size(); ++i ) {
            NBTBase page = list.get(i);
            b.append(((NBTTagString)page).toString() + "\n");
        }
        
        return b.toString();
        
    }
    
    
    
    
}
