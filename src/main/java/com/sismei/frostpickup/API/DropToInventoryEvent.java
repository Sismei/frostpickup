package com.sismei.frostpickup.API;

import java.util.ArrayList;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class DropToInventoryEvent extends Event {
    private final ArrayList<ItemStack> items;

    private final Player player;

    private boolean cancelled = false;

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public DropToInventoryEvent(Player player, ArrayList<ItemStack> items) {
        this.player = player;
        this.items = items;
    }

    public ArrayList<ItemStack> getItems() {
        return this.items;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public Player getPlayer() {
        return this.player;
    }
}
