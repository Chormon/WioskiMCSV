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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.chormon.WioskiMCSV.Config;
import pl.chormon.WioskiMCSV.WioskiMCSV;
import pl.chormon.WioskiMCSV.worldguard.WorldGuard;

/**
 *
 * @author Chormon
 */
public class Wioska {

    private static WioskiMCSV plugin;

    private String nazwa;
    private String akronim;
    private List<String> members;
    private String leader;
    private String world;
    private BlockVector pos1;
    private BlockVector pos2;
    private Date expired;
    private Date estimated;

    public Wioska(String nazwa, String akronim) {
        this.nazwa = nazwa;
        this.akronim = akronim;
    }

    public static void initWioski(WioskiMCSV main) {
        plugin = main;
    }

    public static void stworzWioske(Player player, String nazwa, String akronim) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);
        for (String s : wioski) {
            if (s.toLowerCase().equals(akronim.toLowerCase())) {
                player.sendMessage(Config.getMessage("nieStworzonoWioski", nazwa, akronim));
                player.sendMessage(Config.getMessage("wioskaIstnieje", s));
                return;
            }
        }
        Wioska wioska = new Wioska(nazwa, akronim);
        wioska.setLeader(player.getName());
        wioska.setMembers(new ArrayList<String>());
        wioska.setWorld(player.getLocation().getWorld().getName());

        Location pLocation = player.getLocation();

        wioska.setPos1(pLocation.getBlockX(), pLocation.getBlockY() - 1, pLocation.getBlockZ());
        wioska.setPos2(pLocation.getBlockX() + Config.getWidth() - 1, pLocation.getBlockY() - 1 + Config.getHeight() - 1, pLocation.getBlockZ() + Config.getLength() - 1);

        wioska.setExpired(new Date(System.currentTimeMillis() + (plugin.getConfig().getInt("settings.extend_time") * 3600000 * 24)));
        wioska.setEstimated(new Date(System.currentTimeMillis()));
        try {
            wioska.createCuboid();
            wioskiFile.addWioska(wioska);
            wioskiFile.saveConfig();
            if (Config.getBroadcast()) {
                Bukkit.getServer().broadcastMessage(Config.getMessage("stworzonoWioske", player.getName(), nazwa, akronim));
            } else {
                player.sendMessage(Config.getMessage("stworzonoWioske", player.getName(), nazwa, akronim));
            }
            return;
        } catch (Exception ex) {
            Logger.getLogger(Wioska.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Message that something really bad has happened and village hasn't been created
    }
    
    private static RegionManager getRegionManager(String world) {
        WorldGuardPlugin wgp = WorldGuard.getWorldGuard(plugin);
        World w = Bukkit.getServer().getWorld(world);
        return wgp.getRegionManager(w);
    }

    private void createCuboid() throws Exception {
        RegionManager regionManager = getRegionManager(world);

        String prefix = Config.getPrefix();
        ProtectedCuboidRegion pr = new ProtectedCuboidRegion(prefix + akronim, getPos1(), getPos2());
        DefaultDomain dd = new DefaultDomain();
        dd.addPlayer(leader);
        for(String s : members) {
            dd.addPlayer(s);
        }
        pr.setMembers(dd);
        
        pr.setFlag(DefaultFlag.USE, State.DENY);
        regionManager.addRegion(pr);
    }
    
    public static void checkExpireTime() {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);
        for (String s : wioski) {
                SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date exp = null;
                Date now = new Date(System.currentTimeMillis());
                try {
                    exp = sdt.parse(cs.getString(s + ".do"));
                } catch (ParseException ex) {
                    Logger.getLogger(Wioska.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(exp.before(now)) {
                    delete(s);
                }
            }
    }

    public static void lista(CommandSender sender) {
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
            sender.sendMessage(Config.getMessage("listaWiosekPkt", s, n));
        }
    }

    public static void top(CommandSender sender) {

    }

    public static boolean goodPlace(Location playerLocation) {

        return true;
    }

    public void destroy() {
    }

    public void saveWioska() {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        wioskiFile.editWioska(this);
    }

    public static Boolean addPlayer(String member, Player sender) {
        Wioska w = Wioska.getWioska(sender);
        if (w == null) {
            return false;
        }

        if (!w.addPlayer(member)) {
            return false;
        }

        if (Config.getBroadcast()) {
            Bukkit.getServer().broadcastMessage(Config.getMessage("usunietoUsera", member, w.getAkronim()));
        } else {
            sender.sendMessage(Config.getMessage("usunietoUsera", member, w.getAkronim()));
        }
        return true;
    }

    public static Boolean removePlayer(String member, Player sender) {
        Wioska w = Wioska.getWioska(sender);
        if (w == null) {
            return false;
        }

        if (!w.removePlayer(member)) {
            return false;
        }

        if (Config.getBroadcast()) {
            Bukkit.getServer().broadcastMessage(Config.getMessage("usunietoUsera", member, w.getAkronim()));
        } else {
            sender.sendMessage(Config.getMessage("usunietoUsera", member, w.getAkronim()));
        }

        return true;
    }

    public static void removePlayer(Player sender) {
        Wioska w = Wioska.getWioska(sender);
        if (w == null) {
            sender.sendMessage(Config.getMessage("nieJestesWiosce"));
            return;
        }

        if (sender.getName().equals(w.getLeader())) {
            return;
        }

        if (!w.removePlayer(sender.getName())) {
            return;
        }

        if (Config.getBroadcast()) {
            Bukkit.getServer().broadcastMessage(Config.getMessage("usunietoUsera", sender.getName(), w.getAkronim()));
        } else {
            sender.sendMessage(Config.getMessage("usunietoUsera", sender.getName(), w.getAkronim()));
        }
    }

    public Boolean addPlayer(String name) {
        Player p = Bukkit.getServer().getPlayer(name);
        if (p == null) {
            return false;
        }
        if (p.getName().equals(leader)) {
            return false;
        }
        members.add(name);

        /* Dodanie gracza do cuboidu i configu */
        RegionManager regionManager = getRegionManager(world);
        String prefix = Config.getPrefix();
        DefaultDomain dd = regionManager.getRegion(prefix + akronim).getMembers();
        dd.addPlayer(name);
        regionManager.getRegion(prefix + akronim).setMembers(dd);
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException ex) {
            Logger.getLogger(Wioska.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        saveWioska();
        return true;
    }

    public Boolean removePlayer(String name) {
        if (!members.contains(name)) {
            return false;
        }
        members.remove(name);

        /* Usunięcie gracza z cuboidu i configu */
        RegionManager regionManager = getRegionManager(world);
        String prefix = Config.getPrefix();
        DefaultDomain dd = regionManager.getRegion(prefix + akronim).getMembers();
        dd.removePlayer(name);
        regionManager.getRegion(prefix + akronim).setMembers(dd);
        try {
            regionManager.save();
        } catch (ProtectionDatabaseException ex) {
            Logger.getLogger(Wioska.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        saveWioska();
        return true;
    }

    public static Wioska getWioska(Player player) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);

        for (String s : wioski) {
            String leader = wioskiFile.getConfig().getString("wioski." + s + ".przywodca");
            if (leader.equals(player.getName())) {
                return getWioska(s);
            }
            List<String> members = wioskiFile.getConfig().getStringList("wioski." + s + ".czlonkowie");
            if (members.contains(player.getName())) {
                return getWioska(s);
            }
        }
        return null;
    }

    public static String getAkronim(Player player) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);

        for (String s : wioski) {
            String leader = wioskiFile.getConfig().getString("wioski." + s + ".przywodca");
            if (leader.equals(player.getName())) {
                return s;
            }
            List<String> members = wioskiFile.getConfig().getStringList("wioski." + s + ".czlonkowie");
            if (members.contains(player.getName())) {
                return s;
            }
        }
        return null;
    }

    public static Wioska getWioska(String akronim) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        ConfigurationSection cs = wioskiFile.getConfig().getConfigurationSection("wioski");
        Set<String> wioski = cs.getKeys(false);

        for (String s : wioski) {
            if (s.toLowerCase().equals(akronim.toLowerCase())) {
                Wioska w = new Wioska(cs.getString(s + ".nazwa"), s);

                w.setLeader(cs.getString(s + ".przywodca"));
                w.setMembers(cs.getStringList(s + ".czlonkowie"));

                SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date est = null;
                try {
                    est = sdt.parse(cs.getString(s + ".zalozono"));
                } catch (ParseException ex) {
                    Logger.getLogger(Wioska.class.getName()).log(Level.SEVERE, null, ex);
                }
                w.setEstimated(est);

                Date exp = null;
                try {
                    exp = sdt.parse(cs.getString(s + ".do"));
                } catch (ParseException ex) {
                    Logger.getLogger(Wioska.class.getName()).log(Level.SEVERE, null, ex);
                }
                w.setExpired(exp);
                List<Integer> pt1 = cs.getIntegerList(s + ".lokacja.pos1");
                List<Integer> pt2 = cs.getIntegerList(s + ".lokacja.pos2");
                w.setPos1(pt1.get(0), pt1.get(1), pt1.get(2));
                w.setPos2(pt2.get(0), pt2.get(1), pt2.get(2));
                w.setWorld(cs.getString(s + ".lokacja.world"));

                return w;
            }
        }

        return null;
    }
    
    private static void delete(String akronim) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        Wioska w = getWioska(akronim);
        if (w != null) {
                w.delete();
                if (Config.getBroadcast()) {
                    Bukkit.getServer().broadcastMessage(Config.getMessage("rozwiazanoWioske", w.getAkronim(), w.getNazwa()));
                }
                plugin.getLogger().log(Level.INFO, Config.getMessage("rozwiazanoWioske", w.getAkronim(), w.getNazwa()));
        }
    }

    public static void delete(Player player) {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        Wioska w = getWioska(player);
        if (w != null) {
            if (w.getLeader().equals(player.getName())) {
                w.delete();
                if (Config.getBroadcast()) {
                    Bukkit.getServer().broadcastMessage(Config.getMessage("rozwiazanoWioske", w.getAkronim(), w.getNazwa()));
                } else {
                    player.sendMessage(Config.getMessage("rozwiazanoWioske", w.getAkronim(), w.getNazwa()));
                }
            }
        } else {
            player.sendMessage(Config.getMessage("nieJestesWiosce"));
        }
    }

    public void delete() {
        deleteCuboid();
        WioskiFile wioskiFile = plugin.getWioskiFile();
        wioskiFile.deleteWioska(this.getAkronim());
    }

    public void deleteCuboid() {
        WioskiFile wioskiFile = plugin.getWioskiFile();
        WorldGuardPlugin wgp = WorldGuard.getWorldGuard(plugin);
        World w = Bukkit.getServer().getWorld(world);
        RegionManager regionManager = wgp.getRegionManager(w);

        String prefix = Config.getPrefix();
        regionManager.removeRegion(prefix + getAkronim());
    }

    public static boolean info(Player player) {
        Wioska w = getWioska(player);
        if (w == null) {
            return false;
        }
        w.showInfo(player);
        return true;
    }

    public static boolean info(CommandSender sender, String akronim) {
        Wioska w = getWioska(akronim);
        if (w == null) {
            return false;
        }
        w.showInfo(sender);
        return true;
    }

    public void showInfo(CommandSender sender) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> ite = members.iterator();
        while (ite.hasNext()) {
            sb.append(ite.next());
            if (ite.hasNext()) {
                sb.append(", ");
            }
        }
        String pos = "(" + pos1.getBlockX() + ", " + pos1.getBlockY() + ", " + pos1.getBlockZ() + ") => " + "(" + pos2.getBlockX() + ", " + pos2.getBlockY() + ", " + pos2.getBlockZ() + ")";
        sender.sendMessage(Config.getMessage("infoWioska", akronim, nazwa, leader, sb.toString(), world, pos, getEstimated(), getExpired()));

    }

    public void extend() {
        Date newDate = new Date(this.expired.getTime() + (plugin.getConfig().getInt("settings.extend_time") * 3600000 * 24));
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

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
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

    public BlockVector getPos1() {
        return pos1;
    }

    public void setPos1(BlockVector pos1) {
        this.pos1 = pos1;
    }

    public void setPos1(int x, int y, int z) {
        this.pos1 = new BlockVector(x, y, z);
    }

    public BlockVector getPos2() {
        return pos2;
    }

    public void setPos2(BlockVector pos2) {
        this.pos2 = pos2;
    }

    public void setPos2(int x, int y, int z) {
        this.pos2 = new BlockVector(x, y, z);
    }

}
