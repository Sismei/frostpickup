package com.sismei.frostpickup.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sismei.frostpickup.FrostPickup;
import com.sismei.frostpickup.SuperLoc;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class MythicListener implements Listener {
    private Map<Integer, String> damageMap = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onKill(EntityDeathEvent e) {
        int id = e.getEntity().getEntityId();
        Player killer = null;
        if (this.damageMap.containsKey(Integer.valueOf(id)))
            killer = Bukkit.getPlayer(this.damageMap.get(Integer.valueOf(id)));
        if (killer == null || FrostPickup.getBlockedWorlds().contains(killer.getWorld()))
            return;
        List<Location> locs = new ArrayList<>();
        Location loc = e.getEntity().getLocation();
        locs.add(loc);
        locs.add(loc.clone().add(0.0D, 0.0D, 0.5D));
        locs.add(loc.clone().add(0.0D, 0.0D, -0.5D));
        locs.add(loc.clone().add(0.5D, 0.0D, 0.0D));
        locs.add(loc.clone().add(-0.5D, 0.0D, 0.0D));
        locs.add(loc);
        locs.add(loc);
        for (Location location : locs)
            SuperLoc.add(location, killer, true, FrostPickup.autoSmelt.contains(killer.getName()), FrostPickup.autoBlock.contains(killer.getName()), null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        Player damager;
        if (!FrostPickup.autoMob || e.getEntity() instanceof Player)
            return;
        if (e.getDamager() instanceof Player) {
            damager = (Player)e.getDamager();
        } else if (e.getDamager() instanceof Projectile && ((Projectile)e.getDamager()).getShooter() instanceof Player) {
            damager = (Player)((Projectile)e.getDamager()).getShooter();
        } else {
            return;
        }
        this.damageMap.put(Integer.valueOf(e.getEntity().getEntityId()), damager.getName());
    }
}
