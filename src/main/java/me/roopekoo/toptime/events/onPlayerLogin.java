package me.roopekoo.toptime.events;

import me.roopekoo.toptime.PlayerData;
import me.roopekoo.toptime.TimeTracker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class onPlayerLogin implements Listener {
	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();

	@EventHandler public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		UUID uuid = p.getUniqueId();
		if(!p.hasPlayedBefore()) {
			playerData.addNewPlayer(uuid, p.getName(), 0);
		} else {
			playerData.setOnline(uuid, true);
		}
	}

	@EventHandler public void onLeave(PlayerQuitEvent e) {
		playerData.setOnline(e.getPlayer().getUniqueId(), false);
	}
}
