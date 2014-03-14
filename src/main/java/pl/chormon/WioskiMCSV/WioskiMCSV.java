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

import pl.chormon.WioskiMCSV.wioski.WioskiFile;
import pl.chormon.WioskiMCSV.wioski.Wioska;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import java.math.BigDecimal;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author Chormon
 */
public class WioskiMCSV extends JavaPlugin {

    private WioskiFile wioskiFile;

    public WioskiFile getWioskiFile() {
        return wioskiFile;
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdf = this.getDescription();
        HandlerList.unregisterAll(this);
        getLogger().log(Level.INFO, "{0} {1} disabled!", new Object[]{pdf.getName(), pdf.getVersion()});
    }

    @Override
    public void onEnable() {
        PluginDescriptionFile pdf = this.getDescription();
        //        new PlayerTagListener(this);
        Config.initConfig(this);
        Wioska.initWioski(this);
        wioskiFile = new WioskiFile(this, "wioski.yml");
        getLogger().log(Level.INFO, "{0} {1} enabled!", new Object[]{pdf.getName(), pdf.getVersion()});
        
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                getLogger().log(Level.INFO, "Sprawdzam waznosc wiosek!");
                Wioska.checkExpireTime();
            }
        }, 0L, 72000L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("wioska")) {
            if (args.length > 0) {
                /* KOMENDY GRACZA */
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    /* Tworzenie wioski */
                    if (args[0].equalsIgnoreCase("stworz")) {
                        if (args.length < 3) {
                            sender.sendMessage(Config.getMessage("notEnoughParams"));
                            return false;
                        }
                        String akronim = args[1];
                        /* Tworzenie zablokowane */
                        if (!Config.getCreate()) {
                            sender.sendMessage(Config.getMessage("createDisabled"));
                            return true;
                        }
                        /* Gracz należy już do wioski */
                        if (Wioska.getWioska(player) != null) {
                            sender.sendMessage(Config.getMessage("jestesWiosce"));
                            return true;
                        }
                        /* Istnieje już taka wioska */
                        if (Wioska.getWioska(akronim) != null) {
                            sender.sendMessage(Config.getMessage("wioskaIstnieje", akronim));
                            return true;
                        }

                        /* Czy ma kase */
                        BigDecimal money = new BigDecimal(0);
                        BigDecimal fee = new BigDecimal(Config.getCreateFee());
                        try {
                            money = Economy.getMoneyExact(player.getName());
                        } catch (UserDoesNotExistException ex) {
                            Logger.getLogger(WioskiMCSV.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (money.compareTo(fee) == -1) {
                            sender.sendMessage(Config.getMessage("notEnoughMoney"));
                            return true;
                        }

                        /* Czy dobra odległość */
                        if (!Wioska.goodPlace(player.getLocation())) {
                            sender.sendMessage(Config.getMessage("zlaLokacja"));
                            return true;
                        }

                        StringBuilder concat = new StringBuilder();
                        for (int i = 2; i < args.length; i++) {
                            concat.append(args[i]);
                            if (i + 1 < args.length) {
                                concat.append(" ");
                            }
                        }
                        String nazwa = concat.toString();
                        Wioska.stworzWioske(player, nazwa, akronim);
                        try {
                            Economy.substract(player.getName(), fee);
                        } catch (UserDoesNotExistException ex) {
                            Logger.getLogger(WioskiMCSV.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (NoLoanPermittedException ex) {
                            Logger.getLogger(WioskiMCSV.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ArithmeticException ex) {
                            Logger.getLogger(WioskiMCSV.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        return true;
                        /* Dodawanie gracza do wioski */
                    } else if (args[0].equalsIgnoreCase("dodaj")) {
                        if (args.length < 2) {
                            sender.sendMessage(Config.getMessage("notEnoughParams"));
                            return false;
                        } else if (args.length > 2) {
                            sender.sendMessage(Config.getMessage("tooManyParams"));
                            return false;
                        }
                        return true;
                        /* Usuwanie gracza z wioski */
                    } else if (args[0].equalsIgnoreCase("usun")) {
                        if (args.length < 2) {
                            sender.sendMessage(Config.getMessage("notEnoughParams"));
                            return false;
                        } else if (args.length > 2) {
                            sender.sendMessage(Config.getMessage("tooManyParams"));
                            return false;
                        }

                        return true;
                        /* Rozwiązanie wioski */
                    } else if (args[0].equalsIgnoreCase("rozwiaz")) {
                        if (args.length > 1) {
                            sender.sendMessage(Config.getMessage("tooManyParams"));
                            return false;
                        }
                        Wioska.delete(player);
                        return true;
                        /* Przedłużanie najmu */
                    } else if (args[0].equalsIgnoreCase("przedluz")) {
                        if (args.length > 1) {
                            sender.sendMessage(Config.getMessage("tooManyParams"));
                            return false;
                        }

                        return true;
                        /* Opuszczenie wioski */
                    } else if (args[0].equalsIgnoreCase("opusc")) {
                        if (args.length > 1) {
                            sender.sendMessage(Config.getMessage("tooManyParams"));
                            return false;
                        }
                        Wioska.removePlayer(player);
                        return true;
                    } else if (args[0].equalsIgnoreCase("tak")) {
                    } else if (args[0].equalsIgnoreCase("nie")) {
                    }
                }
                /* KOMENDY GRACZA I KONSOLI */
                /* Lista wiosek */
                if (args[0].equalsIgnoreCase("lista")) {
                    if (args.length > 1) {
                        sender.sendMessage(Config.getMessage("tooManyParams"));
                        return false;
                    }
                    Wioska.lista(sender);
                    return true;
                    /* Informacje o wioskach */
                } else if (args[0].equalsIgnoreCase("info")) {
                    if (args.length < 2) {
                        if (sender instanceof Player) {
                            if(!Wioska.info((Player) sender))
                                sender.sendMessage(Config.getMessage("nieJestesWiosce"));
                            return true;
                        } else {
                            sender.sendMessage(Config.getMessage("notEnoughParams"));
                            return false;
                        }
                    } else if (args.length > 2) {
                        sender.sendMessage(Config.getMessage("tooManyParams"));
                        return false;
                    }
                    if(!Wioska.info(sender, args[1]))
                        sender.sendMessage(Config.getMessage("wioskaNieIstnieje", args[1]));
                        
                    return true;
                    /* Lista człolnków wioski */
                } else if (args[0].equalsIgnoreCase("czlonkowie")) {
                    if (args.length < 2) {
                        if (sender instanceof Player) {

                        } else {
                            sender.sendMessage(Config.getMessage("notEnoughParams"));
                            return false;
                        }
                    } else if (args.length > 2) {
                        sender.sendMessage(Config.getMessage("tooManyParams"));
                        return false;
                    }

                    return true;
                }
//                else {
//                    sender.sendMessage(Config.getMessage("onlyPlayer"));
//                    return true;
//                }
            }
//            /* Lista wiosek */
//        } else if (cmd.getName().equalsIgnoreCase("wioski")) {
//            if (args.length > 0) {
//                sender.sendMessage(Config.getMessage("tooManyParams"));
//                return false;
//            }
//            Wioska.lista(sender);
//            return true;
        }
        return false;
    }
}
