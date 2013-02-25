package com.basicer.parchment.craftbukkit;



import java.lang.reflect.Field;

import org.bukkit.Material;


import com.basicer.parchment.unsafe.ParchmentNBTTagCompound;
import com.basicer.parchment.unsafe.ParchmentNBTTagCompoundImpl;



/**
 *
 * @author basicer
 */
public class Book {
   
    private org.bukkit.inventory.ItemStack base;
    
    public static String readSpell(org.bukkit.inventory.ItemStack itm) {
    	if ( itm == null ) return null;
    	 
    	ParchmentNBTTagCompound tag = ParchmentNBTTagCompoundImpl.getTag(itm, false);
    	if ( tag == null ) return null;
    	

    	return tag.getString("binding");
    	
    }
    
    
    public static void setSpell(org.bukkit.inventory.ItemStack cis, String value) {
    	ParchmentNBTTagCompoundImpl.getTag(cis, true).setString("binding", value);
	    	
    }
    
    /*
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
    */
    
    
    
    
}
