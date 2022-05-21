package com.sismei.frostpickup;

import org.bukkit.inventory.ItemStack;

public class AutoResult {
    private final ItemStack newItem;

    private final ItemStack original;

    private final boolean changed;

    public AutoResult(ItemStack newItem, ItemStack original, boolean changed) {
        this.newItem = newItem;
        this.original = original;
        this.changed = changed;
    }

    public ItemStack getNewItem() {
        return this.newItem;
    }

    public boolean isChanged() {
        return this.changed;
    }

    public ItemStack getOriginal() {
        return this.original;
    }
}
