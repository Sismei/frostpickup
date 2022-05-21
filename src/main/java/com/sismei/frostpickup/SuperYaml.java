package com.sismei.frostpickup;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class SuperYaml {
    private String fileLocation;

    public YamlConfiguration config;

    public SuperYaml(String fileLocation) {
        this.fileLocation = fileLocation;
        reload();
    }

    public void reload() {
        this.config = Config.Load(this.fileLocation);
    }

    public void save() {
        Config.Save(this.config, this.fileLocation);
    }

    public void set(String path, Object value) {
        if (value instanceof Location) {
            setLocation(path, (Location)value);
        } else {
            this.config.set(path, value);
        }
    }

    public Object get(String path) {
        return this.config.get(path);
    }

    public int getInt(String path) {
        return this.config.getInt(path);
    }

    public Boolean getBoolean(String path) {
        return Boolean.valueOf(this.config.getBoolean(path));
    }

    public double getDouble(String path) {
        return this.config.getDouble(path);
    }

    public ItemStack getItemStack(String path) {
        return this.config.getItemStack(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return this.config.getConfigurationSection(path);
    }

    public String getString(String path) {
        return this.config.getString(path);
    }

    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public Location getLocation(String name) {
        if (this.config.get(name) == null || Bukkit.getWorld(this.config.getString(name + ".World")) == null)
            return null;
        Location loc = new Location(Bukkit.getWorld(this.config.getString(name + ".World")), this.config.getDouble(name + ".X"), this.config.getDouble(name + ".Y"), this.config.getDouble(name + ".Z"), (float)this.config.getDouble(name + ".Yaw"), (float)this.config.getDouble(name + ".Pitch"));
        return loc.add(0.0D, 0.1D, 0.0D);
    }

    public void setLocation(String name, Location loc) {
        set(name + ".World", loc.getWorld().getName());
        set(name + ".X", Double.valueOf(loc.getX()));
        set(name + ".Y", Double.valueOf(loc.getY()));
        set(name + ".Z", Double.valueOf(loc.getZ()));
        set(name + ".Yaw", Float.valueOf(loc.getYaw()));
        set(name + ".Pitch", Float.valueOf(loc.getPitch()));
    }
}
