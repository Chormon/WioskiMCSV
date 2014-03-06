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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.chormon.WioskiMCSV.Config;
import pl.chormon.WioskiMCSV.WioskiMCSV;

/**
 *
 * @author Chormon
 */
public class Wioska {
    
    private static WioskiMCSV plugin;

    private String nazwa;
    private String akronim;
    private HashMap<String, Player> members;
    private Player leader;
    private World world;
    private int x, y, z;
    private Date expired;
    private Date estimated;

    public Wioska(String nazwa, String akronim) {
        this.nazwa = nazwa;
        this.akronim = akronim;
    }
    
    public static void initWioski(WioskiMCSV main) {
        plugin = main;
    }
    
    public static String playerWioska(Player player) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);
        
        for (String s : wioski) {
            List<String> members = wioskiFile.getConfig().getStringList("wioski."+s+".members");
            if(members.contains(player.getName()))
                return s;
        }        
        return null;        
    }

    public static void StworzWioske(Player player, String nazwa, String akronim) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);

        for (String s : wioski) {
            if (s.equals(akronim)) {
                player.sendMessage(Config.getMessage("nieStworzonoWioski", nazwa, akronim));
                player.sendMessage(Config.getMessage("wioskaIstnieje", s));
                return;
            }
        }        
        Bukkit.getServer().broadcastMessage(Config.getMessage("stworzonoWioske", player.getName(), nazwa, akronim));
        Wioska wioska = new Wioska(nazwa, akronim);
        wioska.setLeader(player);
        wioska.setWorld(player.getLocation().getWorld());
        wioska.setX(player.getLocation().getBlockX());
        wioska.setY(player.getLocation().getBlockY());
        wioska.setZ(player.getLocation().getBlockZ());
        wioska.setExpired(new Date(System.currentTimeMillis()+(plugin.getConfig().getInt("settings.extend_time")*3600000*24)));
        wioska.setEstimated(new Date(System.currentTimeMillis()));
        wioskiFile.addWioska(wioska);
        wioskiFile.saveConfig();
        wioskiFile.reloadConfig();
    }

    public static void Lista(CommandSender sender) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");

        sender.sendMessage(Config.getMessage("listaWiosek"));
        Set<String> wioski = cs.getKeys(false);
        if (wioski.size() < 1) {
            sender.sendMessage(Config.getMessage("brakWiosek"));
            return;
        }
        for (String s : wioski) {
            String n = wioskiFile.getConfig().getString("wioski." + s + ".nazwa");
            sender.sendMessage(ChatColor.BLUE+"[" + s + "] " + n);
        }
    }
    
    public static void Top(CommandSender sender) {
//        WioskiFile wioskiFile = plugin.getWioskiFile();
//        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
//
//        sender.sendMessage(Config.getMessage("listaWiosek"));
//        Set<String> wioski = cs.getKeys(false);
//        if (wioski.size() < 1) {
//            sender.sendMessage(Config.getMessage("brakWiosek"));
//            return;
//        }
//        for (String s : wioski) {
//            String n = wioskiFile.getConfig().getString("wioski." + s + ".nazwa");
//            sender.sendMessage(ChatColor.BLUE+"[" + s + "] " + n);
//        }
    }
    
    public static boolean DobraLokacja(Location playerLocation) {
        
        return true;
    }

    public void destroy() {
    }

    public static Wioska getWioska(String akronim) {

        return null;
    }
    
    public static void saveWioska() {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        wioskiFile.saveConfig();
        wioskiFile.reloadConfig();        
    }

    public Boolean addPlayer(String name) {
        Player p = Bukkit.getServer().getPlayer(name);
        if (p == null) {
            return false;
        }
        if (p.getName().equals(leader.getName())) {
            return false;
        }
        members.put(name, p);
        Bukkit.getServer().broadcastMessage(Config.getMessage("dodanoUsera", name, this.getAkronim()));
        saveWioska();
        return true;
    }

    public Boolean removePlayer(String name) {
        if (!members.containsKey(name)) {
            return false;
        }
        members.remove(name);
        Bukkit.getServer().broadcastMessage(Config.getMessage("usunietoUsera", name, this.getAkronim()));
        saveWioska();
        return true;
    }
    
    public void extend() {
        Date newDate = new Date(this.expired.getTime()+(plugin.getConfig().getInt("settings.extend_time")*3600000*24));
        this.expired = newDate;
        saveWioska();
    }

    public String getNazwa() {
        return nazwa;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    public String getAkronim() {
        return akronim;
    }

    public void setAkronim(String akronim) {
        this.akronim = akronim;
    }

    public HashMap<String, Player> getMembers() {
        return members;
    }

    public void setMembers(HashMap<String, Player> members) {
        this.members = members;
    }

    public Player getLeader() {
        return leader;
    }

    public void setLeader(Player leader) {
        this.leader = leader;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public String getExpired() {
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
        return sdt.format(expired);
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public String getEstimated() {
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm"); 
        return sdt.format(estimated);
    }

    public void setEstimated(Date estimated) {
        this.estimated = estimated;
    }
    
    
}
