package com.sismei.frostpickup;

import com.sismei.frostpickup.API.DropToInventoryEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class SuperLoc {
    public static HashMap<Location, SuperLoc> superLocs = new HashMap<>();

    private final Player p;

    private final boolean autoPickup;

    private final boolean autoSmelt;

    private final boolean autoBlock;

    private final ItemStack itemStack;

    private SuperLoc(Player p, boolean autoPickup, boolean autoSmelt, boolean autoBlock, ItemStack is) {
        this.p = p;
        this.autoPickup = autoPickup;
        this.autoSmelt = autoSmelt;
        this.autoBlock = autoBlock;
        this.itemStack = is;
    }

    public static void add(Location loc, Player p, boolean autoPickup, boolean autoSmelt, boolean autoBlock, ItemStack is) {
        final Location location = loc.getBlock().getLocation();
        if (superLocs.containsKey(location))
            superLocs.remove(location);
        final SuperLoc sl = new SuperLoc(p, autoPickup, autoSmelt, autoBlock, is);
        superLocs.put(location, sl);
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)FrostPickup.plugin, new Runnable() {
            public void run() {
                if (SuperLoc.superLocs.containsKey(location) && ((SuperLoc)SuperLoc.superLocs.get(location)).equals(sl))
                    SuperLoc.superLocs.remove(location);
            }
        },  10L);
    }

    public static boolean doStuff(Item item, Location exactLoc) {
        Location loc = exactLoc.getBlock().getLocation();
        if (item == null || !superLocs.containsKey(loc))
            return false;
        SuperLoc sl = superLocs.get(loc);
        if (sl == null || !sl.p.isValid())
            return false;
        ItemStack is = item.getItemStack();
        if (FrostPickup.usingPrisonGems && is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().getDisplayName().contains("Gem"))
            return false;
        boolean fortunify = false;
        if (sl.autoSmelt) {
            item.setItemStack(AutoSmelt.smelt(is).getNewItem());
            if (FrostPickup.smeltFortune && Arrays.<Material>asList(new Material[] { Material.IRON_INGOT, Material.GOLD_INGOT }).contains(item.getItemStack().getType()))
                fortunify = true;
        }
        if (FrostPickup.fortuneList.contains(item.getItemStack().getType()))
            fortunify = true;
        if (fortunify && sl.itemStack != null && sl.itemStack.getEnchantments().containsKey(Enchantment.LOOT_BONUS_BLOCKS)) {
            int level = sl.itemStack.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
            int multiplier = (new Random()).nextInt(level + 2) + 1;
            item.getItemStack().setAmount(item.getItemStack().getAmount() * multiplier);
        }
        if (sl.autoPickup) {
            ArrayList<ItemStack> items = new ArrayList<>();
            items.add(item.getItemStack());
            DropToInventoryEvent die = new DropToInventoryEvent(sl.p, items);
            Bukkit.getServer().getPluginManager().callEvent((Event)die);
            Collection<ItemStack> remaining = new ArrayList<>();
            if (die.isCancelled()) {
                superLocs.remove(loc);
                for (ItemStack spawn : die.getItems())
                    exactLoc.getWorld().dropItem(exactLoc, spawn);
                return true;
            }
            for (ItemStack give : die.getItems()) {
                if (sl.autoBlock) {
                    remaining.addAll(AutoBlock.addItem(sl.p, give).values());
                    continue;
                }
                remaining.addAll(FrostPickup.giveItem(sl.p, give).values());
            }
            if (!remaining.isEmpty()) {
                if (!die.isCancelled())
                    FrostPickup.warn(sl.p);
                if (!FrostPickup.deleteOnFull)
                    return false;
            }
            return true;
        }
        return false;
    }
}
