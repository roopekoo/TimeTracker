package me.roopekoo.timeTracker;

import me.roopekoo.timeTracker.commands.*;
import me.roopekoo.timeTracker.events.onPlayerLogin;
import me.roopekoo.timeTracker.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public final class TimeTracker extends JavaPlugin {
	private static final String BASEDIR = "plugins/TimeTracker";
	private static final String PATH = "/messages.yml";
	private static final File MSG_FILE = new File(BASEDIR+PATH);
	private static TimeTracker plugin = null;
	private final YamlConfiguration MSG;
	private final PlayerData playerData = new PlayerData();

	public TimeTracker() {
		File f = new File(BASEDIR);
		if(!f.exists()) {
			f.mkdir();
		}
		if(!f.exists()) {
			try {
				MSG_FILE.createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		MSG = YamlConfiguration.loadConfiguration(MSG_FILE);
	}

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
		TabCompletition tabcompleter = new TabCompletition();
		Objects.requireNonNull(plugin.getCommand("gettime")).setExecutor(new gettime());
		Objects.requireNonNull(plugin.getCommand("gettime")).setTabCompleter(tabcompleter);
		Objects.requireNonNull(plugin.getCommand("toptime")).setExecutor(new TopTime());
		Objects.requireNonNull(plugin.getCommand("toptime")).setTabCompleter(tabcompleter);
		Objects.requireNonNull(plugin.getCommand("playhistory")).setExecutor(new playHistory());
		Objects.requireNonNull(plugin.getCommand("playhistory")).setTabCompleter(tabcompleter);
		Objects.requireNonNull(plugin.getCommand("topplayhistory")).setExecutor(new topPlayHistory());
		Objects.requireNonNull(plugin.getCommand("topplayhistory")).setTabCompleter(tabcompleter);
		Bukkit.getPluginManager().registerEvents(new onPlayerLogin(), plugin);
		loadMessages();
		TimeTracker.getPlugin().getPlayerData().initializePlayerData();
	}

	private void loadMessages() {
		for(Messages item: Messages.values()) {
			if(MSG.getString(item.getPath()) == null) {
				MSG.set(item.getPath(), item.getDefault());
			}
		}
		Messages.setFile(MSG);
		try {
			MSG.save(getMsgFile());
		} catch(IOException e) {
			Bukkit.getLogger().log(Level.WARNING, Messages.TITLE+Messages.SAVE_FAIL1.toString());
			Bukkit.getLogger().log(Level.WARNING, Messages.TITLE+Messages.SAVE_FAIL2.toString());
			e.printStackTrace();
		}
	}


	private File getMsgFile() {
		return MSG_FILE;
	}

	public PlayerData getPlayerData() {
		return playerData;
	}
}
