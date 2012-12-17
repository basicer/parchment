package com.basicer.parchment.craftbukkit;

import net.minecraft.server.*;

import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author basicer
 */
public class Book {
   
    private net.minecraft.server.ItemStack handle;
    private ItemStack base;
    
    public static Book createFromBukkitItemStack(ItemStack base) {
        return new Book(base);
    }
    
    private Book(ItemStack base) {
        if ( base.getType() != Material.BOOK_AND_QUILL && base.getType() != Material.WRITTEN_BOOK ) {
            throw new java.lang.IllegalArgumentException("Item is not a book.");
        }
        
        if ( !(base instanceof CraftItemStack) ) {
            throw new java.lang.IllegalArgumentException("Not using CraftBucket as server.");
        }
        
        CraftItemStack ci = (CraftItemStack)base;
        handle = ci.getHandle();
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
    
    public boolean isLocked() {
        return base.getType() == Material.WRITTEN_BOOK;
    }
    
    public void unlock() {
        if ( !isLocked() ) return;
        base.setType(org.bukkit.Material.BOOK_AND_QUILL);
        handle.getTag().remove("title");
        handle.getTag().remove("author");
    }
    
    
    
}
