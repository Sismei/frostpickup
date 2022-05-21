package com.sismei.frostpickup;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class AutoBlock {
    public static HashMap<Material, Material> convertTo = new HashMap<>();

    public static HashMap<Material, Integer> convertNum = new HashMap<>();

    private static HashMap<Material, Short> convertDurability = new HashMap<>();

    public static HashMap<Integer, ItemStack> addItem(Player p, ItemStack is) {
        if (is == null)
            return new HashMap<>();
        PlayerInventory playerInventory = p.getInventory();
        Inventory inv = Bukkit.createInventory((InventoryHolder)p, 36);
        inv.setContents(playerInventory.getContents());
        HashMap<Integer, ItemStack> remaining = FrostPickup.giveItem(p, inv, is);
        if (!convertTo.containsKey(is.getType())) {
            playerInventory.setContents(inv.getContents());
            p.updateInventory();
            return remaining;
        }
        if (remaining.size() == 1 && remaining.values().toArray()[0].equals(is))
            return remaining;
        ItemStack[] newCont = block(p, inv.getContents(), is.getType());
        if (newCont != null) {
            playerInventory.setContents(newCont);
        } else {
            playerInventory.setContents(inv.getContents());
        }
        p.updateInventory();
        return remaining;
    }

    public static void block(Player p) {
        ItemStack[] newConts = block(p, p.getInventory().getContents(), null);
        if (newConts == null) {
            p.sendMessage(Message.ERROR0BLOCKED_INVENTORY + "");
        } else {
            p.getInventory().setContents(newConts);
            p.updateInventory();
            p.sendMessage(Message.SUCCESS0BLOCKED_INVENTORY + "");
        }
    }

    private static ItemStack[] block(Player p, ItemStack[] conts, Material forceType) {
        boolean totalChanged = false;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (ItemStack is : conts) {
                if (is != null && AutoBlock.convertTo.containsKey(is.getType()) && (forceType == null || forceType == is.getType()) && (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) && (
                        !convertDurability.containsKey(is.getType()) || is.getDurability() == ((Short)convertDurability.get(is.getType())).shortValue())) {
                    Material type = is.getType();
                    int num = 0;
                    int required = ((Integer)convertNum.get(type)).intValue();
                    for (ItemStack numIS : conts) {
                        if (numIS != null && numIS.getType() == type && (!numIS.hasItemMeta() || !numIS.getItemMeta().hasDisplayName()) && (
                                !convertDurability.containsKey(type) || numIS.getDurability() == ((Short)convertDurability.get(type)).shortValue()))
                            num += numIS.getAmount();
                    }
                    if (num >= required) {
                        Material convertTo = AutoBlock.convertTo.get(type);
                        changed = true;
                        totalChanged = true;
                        int toMake = num / required;
                        num = toMake * required;
                        int tobeUsed = num;
                        for (int i = 0; i < conts.length; i++) {
                            if (conts[i] != null && conts[i].getType() == type && (!conts[i].hasItemMeta() || !conts[i].getItemMeta().hasDisplayName()) && (
                                    !convertDurability.containsKey(type) || conts[i].getDurability() == ((Short)convertDurability.get(type)).shortValue())) {
                                if (conts[i].getAmount() > tobeUsed) {
                                    conts[i].setAmount(conts[i].getAmount() - tobeUsed);
                                    break;
                                }
                                tobeUsed -= conts[i].getAmount();
                                conts[i] = null;
                            }
                        }
                        Inventory inv = Bukkit.createInventory(null, conts.length);
                        inv.setContents(conts);
                        ItemStack toAdd = new ItemStack(convertTo);
                        toAdd.setAmount(type.getMaxStackSize());
                        while (toMake > convertTo.getMaxStackSize()) {
                            FrostPickup.giveItem(p, inv, toAdd);
                            toMake -= type.getMaxStackSize();
                        }
                        toAdd.setAmount(toMake);
                        FrostPickup.giveItem(p, inv, toAdd);
                        conts = inv.getContents();
                    }
                }
            }
        }
        if (totalChanged)
            return conts;
        return null;
    }

    static {
        convertTo.put(Material.CLAY_BALL, Material.CLAY);
        convertNum.put(Material.CLAY_BALL, Integer.valueOf(4));
        convertTo.put(Material.IRON_INGOT, Material.IRON_BLOCK);
        convertNum.put(Material.IRON_INGOT, Integer.valueOf(9));
        convertTo.put(Material.REDSTONE, Material.REDSTONE_BLOCK);
        convertNum.put(Material.REDSTONE, Integer.valueOf(9));
        convertTo.put(Material.DIAMOND, Material.DIAMOND_BLOCK);
        convertNum.put(Material.DIAMOND, Integer.valueOf(9));
        convertTo.put(Material.INK_SACK, Material.LAPIS_BLOCK);
        convertNum.put(Material.INK_SACK, Integer.valueOf(9));
        convertDurability.put(Material.INK_SACK, Short.valueOf((short)4));
        convertTo.put(Material.COAL, Material.COAL_BLOCK);
        convertNum.put(Material.COAL, Integer.valueOf(9));
        convertDurability.put(Material.COAL, Short.valueOf((short)0));
        convertTo.put(Material.EMERALD, Material.EMERALD_BLOCK);
        convertNum.put(Material.EMERALD, Integer.valueOf(9));
        convertTo.put(Material.GOLD_INGOT, Material.GOLD_BLOCK);
        convertNum.put(Material.GOLD_INGOT, Integer.valueOf(9));
    }
}
