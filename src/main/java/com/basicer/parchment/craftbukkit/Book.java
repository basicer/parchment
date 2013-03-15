package com.basicer.parchment.craftbukkit;



import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.Material;


import com.basicer.parchment.Debug;
import com.basicer.parchment.unsafe.ParchmentNBTTagCompound;
import com.basicer.parchment.unsafe.ParchmentNBTTagCompoundImpl;



/**
 *
 * @author basicer
 */
public class Book {
   
    private org.bukkit.inventory.ItemStack base;
    private static HashMap<org.bukkit.inventory.ItemStack, String> bindings;
    
    public static String readSpell(org.bukkit.inventory.ItemStack itm) {
    	if ( bindings == null ) return null;
    	if ( itm == null ) return null;
    	String s = bindings.get(itm);
    	
    	if ( s != null ) return s;
    	
    	ParchmentNBTTagCompound tag = ParchmentNBTTagCompoundImpl.getTag(itm, false);
    	if ( tag == null ) return null;
    	
    	if ( !tag.hasKey("binding") ) return null;
    	return tag.getString("binding");
    	
    }
    
    
    public static void setSpell(org.bukkit.inventory.ItemStack cis, String value) {
    	
    	if ( bindings == null ) bindings = new HashMap<org.bukkit.inventory.ItemStack, String>();
    	bindings.put(cis, value);
    	if ( !cis.hasItemMeta() ) cis.setItemMeta(cis.getItemMeta());
    	
    	try {
    		ParchmentNBTTagCompound tag = ParchmentNBTTagCompoundImpl.getTag(cis, true);
    		Debug.trace("Try Bind...");
    		if ( tag == null ) return;
    		Debug.trace("Blamoo");
    		Debug.trace("binding", value);

    	} catch ( Exception ex ) {
    		
    	}
	    
    }
    
    public static void ensureSpellWritten(org.bukkit.inventory.ItemStack cis) {
    	if ( bindings == null ) return;
    	String s = bindings.get(cis);
    	Debug.trace("Annnd..." + cis.toString());
    	if ( s == null ) return;
    	Debug.trace("Boom... " + s + " / " + cis.getClass().getName());
    	setSpell(cis, s);
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
