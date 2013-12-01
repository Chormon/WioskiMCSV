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
package pl.chormon.WioskiMCSV.tags;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import pl.chormon.WioskiMCSV.wioski.Wioska;
import pl.chormon.WioskiMCSV.WioskiMCSV;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 *
 * @author Chormon
 */
public class PlayerTagListener implements Listener {

    public PlayerTagListener(WioskiMCSV plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNameTag(PlayerReceiveNameTagEvent event) {
        Player player = event.getPlayer();
        Player me = event.getNamedPlayer();
        if(Wioska.playerWioska(player).equals(Wioska.playerWioska(me))) {
            event.setTag(ChatColor.GREEN + player.getName());
        }
    }
}
