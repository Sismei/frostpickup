package com.sismei.frostpickup.listener;

import com.sismei.frostpickup.AutoBlock;
import com.sismei.frostpickup.AutoSmelt;
import com.sismei.frostpickup.FrostPickup;
import com.sismei.frostpickup.SuperLoc;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class MainListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (FrostPickup.FortuneData != null && FrostPickup.fortuneList.contains(e.getBlock().getType())) {
            String worldId = e.getBlock().getWorld().getUID().toString();
            List<String> list = FrostPickup.FortuneData.getStringList(worldId);
            String vecString = e.getBlock().getLocation().toVector().toString();
            if (!list.contains(vecString)) {
                list.add(vecString);
                FrostPickup.FortuneData.set(worldId, list);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void giveBreakXP(BlockBreakEvent e) {
        if (FrostPickup.autoBlockXp && !FrostPickup.getBlockedWorlds().contains(e.getBlock().getWorld())) {
            e.getPlayer().giveExp(e.getExpToDrop());
            e.setExpToDrop(0);
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        try {
            e.getCurrentItem().getData().getItemType();
            if (e.getInventory().getName().equals(ChatColor.AQUA + "Ayarlar")) {
                e.setCancelled(true);
                Player p = (Player)e.getWhoClicked();
                String name = ChatColor.stripColor(e.getCurrentItem().getItemMeta().getDisplayName().toLowerCase());
                if (name.contains("otomatik toplama")) {
                    if (p.hasPermission("AutoPickup.Toggle")) {
                        if (FrostPickup.autoPickup.contains(p.getName())) {
                            FrostPickup.autoPickup.remove(p.getName());
                        } else {
                            FrostPickup.autoPickup.add(p.getName());
                        }
                        FrostPickup.openGui(p);
                    }
                } else if (name.contains("otomatik eritme")) {
                    if (p.hasPermission("AutoSmelt.Toggle")) {
                        if (FrostPickup.autoSmelt.contains(p.getName())) {
                            FrostPickup.autoSmelt.remove(p.getName());
                        } else {
                            FrostPickup.autoSmelt.add(p.getName());
                        }
                        FrostPickup.openGui(p);
                    }
                } else if (name.contains("otomatik blok")) {
                    if (p.hasPermission("AutoBlock.Toggle")) {
                        if (FrostPickup.autoBlock.contains(p.getName())) {
                            FrostPickup.autoBlock.remove(p.getName());
                        } else {
                            FrostPickup.autoBlock.add(p.getName());
                        }
                        FrostPickup.openGui(p);
                    }
                } else if (!name.contains("auto")) {
                    if (name.contains("close")) {
                        p.closeInventory();
                    } else if (name.contains("smelt")) {
                        AutoSmelt.smelt(p);
                    } else if (name.contains("block")) {
                        AutoBlock.block(p);
                    }
                }
            }
        } catch (NullPointerException|ClassCastException nullPointerException) {}
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        if (e.getPlayer().hasPermission("AutoPickup.enabled"))
            FrostPickup.autoPickup.add(e.getPlayer().getName());
        if (e.getPlayer().hasPermission("AutoBlock.enabled"))
            FrostPickup.autoBlock.add(e.getPlayer().getName());
        if (e.getPlayer().hasPermission("AutoSmelt.enabled"))
            FrostPickup.autoSmelt.add(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        FrostPickup.autoPickup.remove(e.getPlayer().getName());
        FrostPickup.autoBlock.remove(e.getPlayer().getName());
        FrostPickup.autoSmelt.remove(e.getPlayer().getName());
        FrostPickup.warnCooldown.remove(e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent e) {
        if (!FrostPickup.getBlockedWorlds().contains(e.getEntity().getWorld()) && SuperLoc.doStuff(e.getEntity(), e.getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKill(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null || e.getEntity() instanceof Player || FrostPickup.getBlockedWorlds().contains(killer.getWorld()))
            return;
        if (FrostPickup.autoMob) {
            ArrayList<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack drop : e.getDrops()) {
                HashMap<Integer, ItemStack> remaining = killer.getInventory().addItem(new ItemStack[] { drop });
                for (ItemStack remainder : remaining.values())
                    newDrops.add(remainder);
            }
            if (!newDrops.isEmpty())
                FrostPickup.warn(killer);
            e.getDrops().clear();
            if (!FrostPickup.deleteOnFull)
                for (ItemStack is : newDrops)
                    e.getDrops().add(is);
        }
        if (FrostPickup.autoMobXP) {
            killer.giveExp(e.getDroppedExp());
            e.setDroppedExp(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFish(PlayerFishEvent e) {
        if (!FrostPickup.getBlockedWorlds().contains(e.getPlayer().getWorld()) && FrostPickup.autoMob && e.getCaught() != null && e.getCaught() instanceof Item) {
            Item item = (Item)e.getCaught();
            Collection<ItemStack> newDrops = e.getPlayer().getInventory().addItem(new ItemStack[] { item.getItemStack() }).values();
            if (!newDrops.isEmpty())
                FrostPickup.warn(e.getPlayer());
            if (FrostPickup.deleteOnFull || newDrops.isEmpty())
                item.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!FrostPickup.autoChest.booleanValue() && e.getBlock().getType().name().contains("CHEST"))
            return;
        ItemStack inhand = e.getPlayer().getItemInHand();
        if (FrostPickup.FortuneData != null) {
            String worldId = e.getBlock().getWorld().getUID().toString();
            List<String> list = FrostPickup.FortuneData.getStringList(worldId);
            String vecString = e.getBlock().getLocation().toVector().toString();
            if (list.contains(vecString)) {
                inhand = null;
                list.remove(vecString);
                FrostPickup.FortuneData.set(worldId, list);
            }
        }
        if (FrostPickup.getBlockedWorlds().contains(e.getPlayer().getWorld()))
            return;
        String name = e.getPlayer().getName();
        SuperLoc.add(e.getBlock().getLocation(), e.getPlayer(), FrostPickup.autoPickup.contains(name), FrostPickup.autoSmelt.contains(name), FrostPickup.autoBlock.contains(name), inhand);
        if (FrostPickup.infinityPick && e.getPlayer().hasPermission("AutoPickup.infinity") && e.getPlayer().getItemInHand() != null && e.getPlayer().getItemInHand().getType().name().contains("PICKAXE")) {
            e.getPlayer().getItemInHand().setDurability((short)1);
            e.getPlayer().updateInventory();
        }
    }
}
