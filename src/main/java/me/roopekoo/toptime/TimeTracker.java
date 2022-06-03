package me.roopekoo.toptime;

import me.roopekoo.toptime.commands.TabCompletition;
import me.roopekoo.toptime.commands.TopTime;
import me.roopekoo.toptime.commands.gettime;
import me.roopekoo.toptime.events.onPlayerLogin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TimeTracker extends JavaPlugin {
	private static TimeTracker plugin = null;
	private final PlayerData playerData = new PlayerData();

	/**
	 Get TimeTracker instance
	 @return this class
	 */
	public static TimeTracker getPlugin() {
		return plugin;
	}

	@Override public void onEnable() {
		// Plugin startup logic
		plugin = this;
		Objects.requireNonNull(plugin.getCommand("toptime")).setExecutor(new TopTime());
		Objects.requireNonNull(plugin.getCommand("toptime")).setTabCompleter(new TabCompletition());
		Objects.requireNonNull(plugin.getCommand("gettime")).setExecutor(new gettime());
		Objects.requireNonNull(plugin.getCommand("gettime")).setTabCompleter(new TabCompletition());
		Bukkit.getPluginManager().registerEvents(new onPlayerLogin(), plugin);
		TimeTracker.getPlugin().getPlayerData().initializePlayerData();
	}

	public PlayerData getPlayerData() {
		return playerData;
	}
}
