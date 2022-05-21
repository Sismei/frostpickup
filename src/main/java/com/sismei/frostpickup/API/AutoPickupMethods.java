package com.sismei.frostpickup.API;

import java.util.ArrayList;
import java.util.Collection;
import com.sismei.frostpickup.AutoBlock;
import com.sismei.frostpickup.FrostPickup;
import com.sismei.frostpickup.AutoResult;
import com.sismei.frostpickup.AutoSmelt;
import com.sismei.frostpickup.SuperLoc;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AutoPickupMethods {
    public static void openGui(Player player) {
        FrostPickup.openGui(player);
    }

    public static void autoGive(Player player, ItemStack item) {
        if (FrostPickup.autoSmelt.contains(player.getName()))
            item = smelt(item).getNewItem();
        if (FrostPickup.autoPickup.contains(player.getName())) {
            ArrayList<ItemStack> items = new ArrayList<>();
            items.add(item);
            DropToInventoryEvent die = new DropToInventoryEvent(player, items);
            Bukkit.getServer().getPluginManager().callEvent(die);
            Collection<ItemStack> remaining = new ArrayList<>();
            if (die.isCancelled()) {
                SuperLoc.superLocs.remove(die.getPlayer().getLocation().getBlock().getLocation());
                for (ItemStack spawn : die.getItems())
                    player.getWorld().dropItem(player.getLocation(), spawn);
                return;
            }
            for (ItemStack give : die.getItems()) {
                if (FrostPickup.autoBlock.contains(player.getName())) {
                    remaining.addAll(AutoBlock.addItem(player, give).values());
                    continue;
                }
            }
            if (!remaining.isEmpty()) {
                if (!die.isCancelled())
                    FrostPickup.warn(player);
                if (!FrostPickup.deleteOnFull)
                    for (ItemStack is : remaining)
                        player.getWorld().dropItem(player.getLocation(), is);
            }
        }
    }

    public static AutoResult smelt(ItemStack item) {
        return AutoSmelt.smelt(item);
    }

    public static void smeltInventory(Player player) {
        AutoSmelt.smelt(player);
    }

    public static void blockInventory(Player player) {
        AutoBlock.block(player);
    }

    public static void addBlockedItem(Player player, ItemStack is) {
        AutoBlock.addItem(player, is);
    }
}
