package me.roopekoo.toptime;

import me.roopekoo.toptime.commands.TabCompletition;
import me.roopekoo.toptime.commands.TopTime;
import me.roopekoo.toptime.commands.gettime;
import me.roopekoo.toptime.events.onPlayerLogin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public final class TimeTracker extends JavaPlugin {
	private static File MSG_FILE;
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
		loadMessages();
		TimeTracker.getPlugin().getPlayerData().initializePlayerData();
	}

	public YamlConfiguration createFile(String filename, File file) {
		File f = new File(getDataFolder(), filename);
		if(!f.exists()) {
			f.mkdir();
			try {
				file.createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		return YamlConfiguration.loadConfiguration(file);
	}

	private void loadMessages() {
		YamlConfiguration MSG = createFile("messages.yml", MSG_FILE);
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
