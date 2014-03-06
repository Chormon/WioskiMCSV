/*
 * Copyright (C) 2013 Chormon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.chormon.WioskiMCSV.wioski;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pl.chormon.WioskiMCSV.WioskiMCSV;

/**
 *
 * @author Chormon
 */
public class WioskiFile {

    private final String fileName;
    private final WioskiMCSV plugin;
    private File configFile;
    private FileConfiguration fileConfiguration;

    public WioskiFile(WioskiMCSV plugin, String fileName) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin cannot be null");
        }
        if (!plugin.isInitialized()) {
            throw new IllegalArgumentException("plugin must be initiaized");
        }
        this.plugin = plugin;
        this.fileName = fileName;
        File dataFolder = plugin.getDataFolder();
        if (dataFolder == null) {
            throw new IllegalStateException();
        }
        this.configFile = new File(plugin.getDataFolder(), fileName);
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    public void reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            this.reloadConfig();
        }
        return fileConfiguration;
    }

    public void saveConfig() {
        if (fileConfiguration == null || configFile == null) {
            return;
        } else {
            try {
                getConfig().save(configFile);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
            }
        }
    }

    public void saveDefaultConfig() {
        if (!configFile.exists()) {
            this.plugin.saveResource(fileName, false);
        }
    }
    
    public void addWioska(Wioska wioska) {
        String section = "wioski"+wioska.getAkronim();
        this.fileConfiguration.createSection(section);
        this.fileConfiguration.set(section+".nazwa", wioska.getNazwa());
        this.fileConfiguration.set(section+".przywodca", wioska.getLeader());
        String[] czlonkowie = (String[]) wioska.getMembers().keySet().toArray();
        this.fileConfiguration.set(section+".czlonkowie", czlonkowie);
        this.fileConfiguration.createSection(section+".lokacja");
        this.fileConfiguration.set(section+".lokacja.world", wioska.getWorld());
        this.fileConfiguration.set(section+".lokacja.x", wioska.getX());
        this.fileConfiguration.set(section+".lokacja.y", wioska.getY());
        this.fileConfiguration.set(section+".lokacja.z", wioska.getZ());
        this.fileConfiguration.set(section+".zalozono", wioska.getEstimated());
        this.fileConfiguration.set(section+".do", wioska.getExpired());
    }
    
    public void editWioska(Wioska wioska) {
        
    }
    
    public void deleteWioska(String nazwa) {
        
    }
}
