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
package pl.chormon.WioskiMCSV;

import java.text.MessageFormat;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

/**
 *
 * @author Chormon
 */
public class Config {

    private static WioskiMCSV plugin;

    public static void initConfig(WioskiMCSV main) {
        main.reloadConfig();
        main.getConfig().options().copyDefaults(true);
        main.saveConfig();
        plugin = main;
    }
    
    public static String getMessage(String path)
    {
        return getMessage(path, (Object) null);
    }

    public static String getMessage(String path, Object... params) {
        String message = plugin.getConfig().getString("messages." + path);
        for (ChatColor color : ChatColor.values()) {
            String key = "$" + color.name().toLowerCase() + "$";
            if (message.contains(key)) {
                message = message.replaceAll(key, color.toString());
            }
        }
        if (params != null) {
            return MessageFormat.format(message, params);
        } else {
            return message;
        }
    }
    
    public static boolean getCreate() {
        return plugin.getConfig().getBoolean("settings.create");
    }
    
    public static ChatColor getAllyColor() {
        String allyColor = plugin.getConfig().getString("settings.ally_color");
        for (ChatColor color : ChatColor.values()) {
            if(color.name().toLowerCase().equals(allyColor)) {
                return color;
            }
        }
        return ChatColor.GREEN;        
    }
    
    public static ChatColor getEnemyColor() {
        String enemyColor = plugin.getConfig().getString("settings.ally_color");
        for (ChatColor color : ChatColor.values()) {
            if(color.name().toLowerCase().equals(enemyColor)) {
                return color;
            }
        }
        return ChatColor.RED;        
    }
    
    public static boolean getPrefix() {
        return plugin.getConfig().getBoolean("settings.prefix");
    }
    
    public static boolean getPrefixTag() {
        if(getPrefix())
            return plugin.getConfig().getBoolean("settings.prefix_tag");
        return false;
    }
    
    public static boolean getTagColor() {
        return plugin.getConfig().getBoolean("settings.tag_color");
    }
    
    public static int getMaxMembers() {
        return plugin.getConfig().getInt("settings.max_members");
    }
    
    public static int getExtendTime() {
        return plugin.getConfig().getInt("settings.extend_time");
    }
    
    public static int getWidth() {
        return plugin.getConfig().getInt("settings.width");
    }
    
    public static int getHeight() {
        return plugin.getConfig().getInt("settings.height");
    }
    
    public static int getLength() {
        return plugin.getConfig().getInt("settings.length");
    }
    
    public static int getRent() {
        return plugin.getConfig().getInt("settings.rent");
    }
    
    public static World[] getWorlds() {
        List<String> list = plugin.getConfig().getStringList("settings.worlds");
        World[] worlds;
        if(!list.isEmpty()) {
            worlds = new World[list.size()];
            for(int i=0; i<list.size(); i++) {
                worlds[i] = Bukkit.getWorld(list.get(i));
            }
            return worlds;
        }
        return (World[]) Bukkit.getWorlds().toArray();
    }
}
