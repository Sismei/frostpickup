package com.sismei.frostpickup;

import com.sismei.frostpickup.listener.MainListener;
import haveric.stackableItems.util.InventoryUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrostPickup extends JavaPlugin {
    public static String dataFolder;

    public static FrostPickup plugin;

    public static boolean usingStackableItems = false;

    public static boolean infinityPick = false, deleteOnFull = true, warnOnFull = false, autoBlockXp = true, autoMob = true, autoMobXP = true, extraInfo = false;

    public static boolean smeltFortune = false;

    public static boolean usingPrisonGems = false;

    public static SuperYaml MainConfig;

    public static SuperYaml MessageConfig;

    public static SuperYaml SmeltConfig;

    public static SuperYaml WorldConfig;

    public static SuperYaml FortuneConfig;

    public static SuperYaml FortuneData = null;

    public static List<String> autoSmelt = new ArrayList<>();

    public static List<String> autoPickup = new ArrayList<>();

    public static List<String> autoBlock = new ArrayList<>();

    public static HashMap<String, Long> warnCooldown = new HashMap<>();

    public static HashMap<Material, Short> smeltBlacklist = new HashMap<>();

    private static List<String> blockedWorlds = new ArrayList<>();

    public static List<Material> fortuneList = new ArrayList<>();

    public static List<String> smeltList = new ArrayList<>();

    public static Boolean allowBlockGui;

    public static Boolean autoChest;

    public static void reloadConfigs() {
        MainConfig = new SuperYaml(dataFolder + "/Config.yml");
        MessageConfig = new SuperYaml(dataFolder + "/Messages.yml");
        SmeltConfig = new SuperYaml(dataFolder + "/Smelt Blacklist.yml");
        WorldConfig = new SuperYaml(dataFolder + "/World Blacklist.yml");
        FortuneConfig = new SuperYaml(dataFolder + "/Advanced Fortune.yml");
        if (FortuneData != null)
            FortuneData.save();
        FortuneData = null;
        Message.setup();
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put("Infinity Pick", Boolean.valueOf(false));
        defaults.put("Gui.Contact Info", Boolean.valueOf(true));
        defaults.put("Full Inventory.Delete Item", Boolean.valueOf(true));
        defaults.put("Full Inventory.Warn", Boolean.valueOf(true));
        defaults.put("AutoSmelt Compat Mode", Boolean.valueOf(true));
        defaults.put("AutoBlock Quartz", Boolean.valueOf(true));
        defaults.put("Mob.AutoPickup", Boolean.valueOf(true));
        defaults.put("Mob.AutoXP", Boolean.valueOf(true));
        defaults.put("Block AutoXP", Boolean.valueOf(true));
        defaults.put("Allow BlockGui Permission", Boolean.valueOf(false));
        defaults.put("Auto Chest", Boolean.valueOf(true));
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (MainConfig.get(entry.getKey()) == null) {
                MainConfig.set(entry.getKey(), entry.getValue());
                MainConfig.save();
            }
        }
        if (SmeltConfig.get("Enable Blacklist") == null) {
            SmeltConfig.set("Enable Blacklist", Boolean.valueOf(true));
            SmeltConfig.save();
        }
        if (FortuneConfig.get("Info") == null) {
            FortuneConfig.set("Info", Arrays.asList(new String[] { "Smelt Fortune means if you have autosmelt on, when you mine something like an iron ore, fortune effects will work on it, meaning you would get more iron ingots if you had fortune", "Fortune all allows to add make fortune work on anything.  For example, you could mine a gold ore with a fortune pick, and get few gold ores as the result.", "To prevent ore duping, this plugin will need to keep a list of placed blocks.  This will require some more RAM and hard drive space (This should only require a few MB)", "The Fortune All Whitelist allows you to determine which blocks are affected by fortune, so you don't get billions of stacks of cobble", "NOTE: The Fortune All Whitelist does not replace the default vanilla fortune whitelist, it just adds to it" }));
            FortuneConfig.save();
        }
        if (FortuneConfig.get("Smelt Fortune") == null) {
            FortuneConfig.set("Smelt Fortune", Boolean.valueOf(true));
            FortuneConfig.save();
        }
        if (FortuneConfig.get("Fortune All") == null) {
            FortuneConfig.set("Fortune All", Boolean.valueOf(false));
            FortuneConfig.save();
        }
        if (FortuneConfig.get("Fortune All Whitelist") == null) {
            FortuneConfig.set("Fortune All Whitelist", Arrays.asList(new String[] { "GOLD_ORE", "IRON_ORE", "DIAMOND_ORE", "LAPIS_ORE", "QUARTZ_ORE", "MYCEL" }));
            FortuneConfig.save();
        }
        if (SmeltConfig.get("Blacklist") == null) {
            SmeltConfig.set("Blacklist", Arrays.asList(new String[] { "1", "Coal:1" }));
            SmeltConfig.save();
        }
        if (WorldConfig.get("Enable Blacklist") == null) {
            WorldConfig.set("Enable Blacklist", Boolean.valueOf(true));
            WorldConfig.save();
        }
        if (WorldConfig.get("Blacklist") == null) {
            WorldConfig.set("Blacklist", Arrays.asList(new String[] { "ExampleWorld", "2nd_Example" }));
            WorldConfig.save();
        }
        if (MainConfig.getBoolean("AutoBlock Quartz").booleanValue()) {
            AutoBlock.convertTo.put(Material.QUARTZ, Material.QUARTZ_BLOCK);
            AutoBlock.convertNum.put(Material.QUARTZ, Integer.valueOf(4));
        } else {
            AutoBlock.convertTo.remove(Material.QUARTZ);
            AutoBlock.convertNum.remove(Material.QUARTZ);
        }
        if (MainConfig.getBoolean("AutoSmelt Compat Mode").booleanValue()) {
            smeltList.clear();
        } else {
            smeltList = Arrays.asList(new String[] {
                    "SMOOTH_BRICK", "RAW_FISH", "REDSTONE_ORE", "POTATO_ITEM", "RAW_CHICKEN", "SPONGE", "DIAMOND_ORE", "LOG", "CACTUS", "RAW_FISH",
                    "LAPIS_ORE", "SAND", "IRON_ORE", "MUTTON", "QUARTZ_ORE", "COAL_ORE", "GOLD_ORE", "NETHERRACK", "LOG_2", "RAW_BEEF",
                    "CLAY_BALL", "COBBLESTONE", "EMERALD_ORE", "RABBIT", "CLAY", "PORK" });
        }
        smeltFortune = FortuneConfig.getBoolean("Smelt Fortune").booleanValue();
        fortuneList.clear();
        if (FortuneConfig.getBoolean("Fortune All").booleanValue()) {
            FortuneData = new SuperYaml(dataFolder + "/Fortune Data");
            for (Object o : FortuneConfig.config.getList("Fortune All Whitelist")) {
                if (o != null) {
                    Material material = Material.matchMaterial(o.toString());
                    if (material == null) {
                        Bukkit.getLogger().severe(o.toString() + "Is not a valid block name in: Advanced Fortune.yml");
                        continue;
                    }
                    fortuneList.add(material);
                }
            }
        }
        extraInfo = MainConfig.getBoolean("Gui.Contact Info").booleanValue();
        blockedWorlds.clear();
        if (WorldConfig.getBoolean("Enable Blacklist").booleanValue())
            for (Object raw : WorldConfig.config.getList("Blacklist")) {
                if (raw instanceof String)
                    blockedWorlds.add((String)raw);
            }
        smeltBlacklist.clear();
        if (SmeltConfig.getBoolean("Enable Blacklist").booleanValue())
            for (Object raw : SmeltConfig.config.getList("Blacklist")) {
                if (!(raw instanceof String))
                    continue;
                String[] split = ((String)raw).split(":");
                Material mat = Material.matchMaterial(split[0]);
                if (mat == null) {
                    Bukkit.getLogger().severe(ChatColor.RED + "[AutoPickup] The blacklist item: '" + split[0] + "' could not be found");
                    continue;
                }
                short data = -1;
                if (split.length > 1)
                    try {
                        data = Short.valueOf(split[1].replace(" ", "")).shortValue();
                    } catch (NumberFormatException ex) {
                        Bukkit.getLogger().severe(ChatColor.RED + "[AutoPickup] The blacklist item: '" + raw + "' does not have a valid data number");
                        data = -1;
                    }
                smeltBlacklist.put(mat, Short.valueOf(data));
            }
        infinityPick = MainConfig.getBoolean("Infinity Pick").booleanValue();
        deleteOnFull = MainConfig.getBoolean("Full Inventory.Delete Item").booleanValue();
        warnOnFull = MainConfig.getBoolean("Full Inventory.Warn").booleanValue();
        autoMob = MainConfig.getBoolean("Mob.AutoPickup").booleanValue();
        autoBlockXp = MainConfig.getBoolean("Block AutoXP").booleanValue();
        autoMobXP = MainConfig.getBoolean("Mob.AutoXP").booleanValue();
        autoChest = MainConfig.getBoolean("AutoChest");
        allowBlockGui = MainConfig.getBoolean("Allow BlockGui Permission");
    }

    public static ItemStack easyItem(String name, Material material, int amount, int durability, String... lore) {
        ItemStack is = new ItemStack(material);
        if (durability > 0)
            is.setDurability((short)durability);
        if (amount > 1)
            is.setAmount(amount);
        if (is.getItemMeta() != null) {
            ItemMeta im = is.getItemMeta();
            if (name != null)
                im.setDisplayName(name);
            if (lore != null) {
                ArrayList<String> loreList = new ArrayList<>();
                Collections.addAll(loreList, lore);
                im.setLore(loreList);
            }
            is.setItemMeta(im);
        }
        return is;
    }

    public void onDisable() {
        if (FortuneData != null)
            FortuneData.save();
    }

    public void onEnable() {
        plugin = this;
        dataFolder = getDataFolder().getAbsolutePath();
        reloadConfigs();
        getServer().getPluginManager().registerEvents((Listener)new MainListener(), (Plugin)this);
        ArrayList<String> plugins = new ArrayList<>();
        if (getServer().getPluginManager().getPlugin("StackableItems") != null) {
            plugins.add("StackableItems");
            usingStackableItems = true;
        }
        if (!plugins.isEmpty()) {
            String message = "[AutoPickup] Detected you are using ";
            for (String pName : plugins) {
                if (!message.endsWith(" "))
                    message = message + ", ";
                message = message + pName;
            }
            Bukkit.getLogger().info(message);
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("AutoPickup.enabled"))
                autoPickup.add(p.getName());
            if (p.hasPermission("AutoBlock.enabled"))
                autoBlock.add(p.getName());
            if (p.hasPermission("AutoSmelt.enabled"))
                autoSmelt.add(p.getName());
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && (args[0].equalsIgnoreCase("rl") || args[0].equalsIgnoreCase("reload"))) {
            reloadCommand(sender);
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You need to be a player to do this!");
            return true;
        }
        Player p = (Player)sender;
        if (getBlockedWorlds().contains(p.getWorld())) {
            p.sendMessage(Message.ERROR0BLACKLISTED0WORLD + "");
        } else {
            switch (cmd.getName()) {
                case "AutoSmelt":
                    if (args.length == 0) {
                        if (!p.hasPermission("AutoSmelt.command")) {
                            p.sendMessage(Message.ERROR0NO_PERM + "");
                            break;
                        }
                        AutoSmelt.smelt(p);
                        break;
                    }
                    if (args[0].equalsIgnoreCase("toggle")) {
                        if (!p.hasPermission("AutoSmelt.toggle")) {
                            p.sendMessage(Message.ERROR0NO_PERM + "");
                            break;
                        }
                        if (autoSmelt.contains(p.getName())) {
                            autoSmelt.remove(p.getName());
                            p.sendMessage(Message.SUCCESS0TOGGLE0SMELT_OFF + "");
                            break;
                        }
                        autoSmelt.add(p.getName());
                        p.sendMessage(Message.SUCCESS0TOGGLE0SMELT_ON + "");
                        break;
                    }
                    displayHelp((CommandSender)p);
                    break;
                case "AutoPickup":
                    if (args.length == 0) {
                        openGui(p);
                        break;
                    }
                    if (args[0].equalsIgnoreCase("toggle")) {
                        if (!p.hasPermission("AutoPickup.toggle")) {
                            p.sendMessage(Message.ERROR0NO_PERM + "");
                            break;
                        }
                        if (autoPickup.contains(p.getName())) {
                            autoPickup.remove(p.getName());
                            p.sendMessage(Message.SUCCESS0TOGGLE0PICKUP_OFF + "");
                            break;
                        }
                        autoPickup.add(p.getName());
                        p.sendMessage(Message.SUCCESS0TOGGLE0PICKUP_ON + "");
                        break;
                    }
                    displayHelp((CommandSender)p);
                    break;
                case "AutoBlock":
                    if (args.length == 0) {
                        if (!p.hasPermission("AutoBlock.command")) {
                            p.sendMessage(Message.ERROR0NO_PERM + "");
                            break;
                        }
                        AutoBlock.block(p);
                        break;
                    }
                    if (args[0].equalsIgnoreCase("toggle")) {
                        if (!p.hasPermission("AutoBlock.toggle")) {
                            p.sendMessage(Message.ERROR0NO_PERM + "");
                            break;
                        }
                        if (autoBlock.contains(p.getName())) {
                            autoBlock.remove(p.getName());
                            p.sendMessage(Message.SUCCESS0TOGGLE0BLOCK_OFF + "");
                            break;
                        }
                        autoBlock.add(p.getName());
                        p.sendMessage(Message.SUCCESS0TOGGLE0BLOCK_ON + "");
                        break;
                    }
                    displayHelp((CommandSender)p);
                    break;
            }
        }
        return true;
    }

    public static void openGui(Player p) {
        if (allowBlockGui.booleanValue() && p.hasPermission("AutoPickup.BlockGui")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to open the gui");
            return;
        }
        int size = 18;
        Inventory newInv = Bukkit.createInventory(null, size, ChatColor.AQUA + "Ayarlar");
        ItemStack[] conts = newInv.getContents();
        conts[3] = easyItem(ChatColor.GREEN + "Otomatik Toplama", Material.HOPPER, 1, 0, new String[] { ChatColor.GRAY + "Kırmış olduğunuz bloklar", ChatColor.GRAY + "otomatik olarak envanterinize aktarılır." });
        conts[4] = easyItem(ChatColor.GREEN + "Otomatik Blok", Material.IRON_BLOCK, 1, 0, new String[] { ChatColor.GRAY + "Kırmış olduğunuz bloklar", ChatColor.GRAY + "otomatik olarak blok yapılır.." });
        conts[5] = easyItem(ChatColor.GREEN + "Otomatik Eritme", Material.FURNACE, 1, 0, new String[] { ChatColor.GRAY + "Kırmış olduğunuz bloklar", ChatColor.GRAY + "otomatik olarak eritilir." });
        String autoPickupName = autoPickup.contains(p.getName()) ? (ChatColor.GRAY + "Otomatik Toplama" + " §aAktif") : (ChatColor.GRAY + "Otomatik Toplama" + " §cDevredışı");
        String autoBlockName = autoBlock.contains(p.getName()) ? (ChatColor.GRAY + "Otomatik Blok" + " §aAktif") : (ChatColor.GRAY + "Otomatik Blok" + " §cDevredışı");
        String autoSmeltName = autoSmelt.contains(p.getName()) ? (ChatColor.GRAY + "Otomatik Eritme" + " §aAktif") : (ChatColor.GRAY + "Otomatik Eritme" + " §cDevredışı");
        int apDur = autoPickup.contains(p.getName()) ? 10 : 8;
        int abDur = autoBlock.contains(p.getName()) ? 10 : 8;
        int asDur = autoSmelt.contains(p.getName()) ? 10 : 8;
        conts[12] = p.hasPermission("AutoPickup.Toggle") ? easyItem(autoPickupName, Material.INK_SACK, 1, apDur, new String[] { ChatColor.GRAY + "Modu değiştirmek için tıkla." }) : easyItem(autoPickupName, Material.INK_SACK, 1, apDur, new String[0]);
        conts[13] = p.hasPermission("AutoBlock.Toggle") ? easyItem(autoBlockName, Material.INK_SACK, 1, abDur, new String[] { ChatColor.GRAY + "Modu değiştirmek için tıkla." }) : easyItem(autoBlockName, Material.INK_SACK, 1, abDur, new String[0]);
        conts[14] = p.hasPermission("AutoSmelt.Toggle") ? easyItem(autoSmeltName, Material.INK_SACK, 1, asDur, new String[] { ChatColor.GRAY + "Modu değiştirmek için tıkla." }) : easyItem(autoSmeltName, Material.INK_SACK, 1, asDur, new String[0]);
        ItemStack locked = easyItem(ChatColor.RED + "Kilitli", Material.STAINED_GLASS_PANE, 1, 14, new String[0]);
        ItemStack empty = easyItem((String)null, Material.STAINED_GLASS_PANE, 1, 7, new String[0]);
        for (int i = 0; i < conts.length; ) {
            if (conts[i] == null)
                conts[i] = empty;
            i++;
        }
        if (p.getInventory() != null && p.getInventory().getName() != null && p.getInventory().getName().equals(ChatColor.AQUA + "Ayarlar")) {
            p.getInventory().setContents(conts);
            p.updateInventory();
        } else {
            newInv.setContents(conts);
            p.openInventory(newInv);
        }
    }

    public static List<World> getBlockedWorlds() {
        ArrayList<World> worlds = new ArrayList<>();
        for (String s : blockedWorlds) {
            World w = Bukkit.getWorld(s);
            if (w != null)
                worlds.add(w);
        }
        return worlds;
    }

    private void reloadCommand(CommandSender s) {
        if (s instanceof Player && !s.hasPermission("AutoSmelt.reload")) {
            s.sendMessage(Message.ERROR0NO_PERM + "");
        } else {
            reloadConfigs();
            s.sendMessage(Message.SUCCESS0RELOADED + "");
        }
    }

    public static void displayHelp(CommandSender s) {
        ChatColor c1 = null;
        ChatColor c2 = null;
        Random random = new Random();
        while (c1 == null || c1 == ChatColor.MAGIC || c1 == ChatColor.ITALIC || c1 == ChatColor.BLACK || c1 == ChatColor.UNDERLINE || c1 == ChatColor.BOLD || c1 == ChatColor.RESET || c1 == ChatColor.STRIKETHROUGH)
            c1 = ChatColor.values()[random.nextInt((ChatColor.values()).length - 1)];
        while (c2 == null || c2 == c1 || c2 == ChatColor.MAGIC || c2 == ChatColor.ITALIC || c2 == ChatColor.BLACK || c2 == ChatColor.UNDERLINE || c2 == ChatColor.BOLD || c2 == ChatColor.RESET || c2 == ChatColor.STRIKETHROUGH)
            c2 = ChatColor.values()[random.nextInt((ChatColor.values()).length - 1)];
        ArrayList<String> messages = new ArrayList<>();
        messages.add("AutoPickup-Displays this screen");
        messages.add("AutoPickup toggle-Toggles auto pickup");
        messages.add("AutoBlock toggle-Toggles auto block");
        messages.add("AutoBlock-Turns anything that can be into a block");
        messages.add("AutoSmelt-Smelts anything that can be smelted in your inventory");
        messages.add("AutoSmelt toggle-Toggles auto smelt");
        messages.add("AutoSmelt reload-Reloads the plugin");
        s.sendMessage(c1 + "==== " + c2 + plugin.getName() + c1 + " ====");
        for (String message : messages)
            s.sendMessage(c2 + "/" + message.replace("-", c1 + " - "));
        s.sendMessage(c1 + "For more help: " + c2 + "http://goo.gl/WdfLpK");
        s.sendMessage(c1 + "Shortcuts: " + c2 + "/ap = /AutoPickup, /ab = /AutoBlock, /as = /AutoSmelt");
    }

    public static void warn(Player p) {
        if (warnOnFull && p != null && p.isValid() && (!warnCooldown.containsKey(p.getName()) || ((Long)warnCooldown.get(p.getName())).longValue() < Calendar.getInstance().getTimeInMillis())) {
            p.sendMessage(Message.ERROR0FULL_INVENTORY + "");
            warnCooldown.put(p.getName(), Long.valueOf(5000L + Calendar.getInstance().getTimeInMillis()));
        }
    }

    public static HashMap<Integer, ItemStack> giveItem(Player p, Inventory inv, ItemStack is) {
        if (is == null)
            return new HashMap<>();
        if (!usingStackableItems || p == null)
            return inv.addItem(new ItemStack[] { is });
        ItemStack toSend = is.clone();
        ItemStack remaining = null;
        int freeSpaces = InventoryUtil.getPlayerFreeSpaces(p, toSend);
        if (freeSpaces < toSend.getAmount()) {
            remaining = toSend.clone();
            remaining.setAmount(toSend.getAmount() - freeSpaces);
            toSend.setAmount(freeSpaces);
        }
        if (toSend.getAmount() > 0)
            InventoryUtil.addItemsToPlayer(p, toSend, "pickup");
        HashMap<Integer, ItemStack> map = new HashMap<>();
        if (remaining != null)
            map.put(Integer.valueOf(0), remaining);
        return map;
    }

    public static HashMap<Integer, ItemStack> giveItem(Player p, ItemStack is) {
        return giveItem(p, (Inventory)p.getInventory(), is);
    }
}
