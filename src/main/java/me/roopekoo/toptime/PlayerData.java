package me.roopekoo.toptime;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class PlayerData {
	//Store players to memory
	private final HashMap<String, User> playerMap = new HashMap<>();
	//Store UUIDs in map where username is the key
	private final HashMap<String, UUID> name2uuid = new HashMap<>();

	long updateTime = System.currentTimeMillis();
	long totalTime = 0;

	public void initializePlayerData() {
		UUID uuid;
		int playTime;
		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
		// Go through all offline players
		for(OfflinePlayer offlinePlayer: offlinePlayers) {
			uuid = offlinePlayer.getUniqueId();
			String name = offlinePlayer.getName();
			playTime = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);

			assert name != null;
			addNewPlayer(uuid, name, playTime);

		}
	}


	public boolean isUserValid(String arg) {
		return name2uuid.containsKey(arg.toLowerCase());
	}

	public long getPlaytime(String username) {
		if(username.equalsIgnoreCase("total")) {
			return totalTime;
		}
		UUID uuid = name2uuid.get(username.toLowerCase());
		User user = playerMap.get(uuid.toString());
		if(user.isOnline) {
			int ticks = Objects.requireNonNull(Bukkit.getPlayer(user.uuid)).getStatistic(Statistic.PLAY_ONE_MINUTE);
			user.playTimeTicks = ticks;
			return ticks;
		}
		return user.playTimeTicks;
	}

	public void setOnline(UUID uuid, boolean b) {
		playerMap.get(uuid.toString()).isOnline = b;
	}

	public void addNewPlayer(UUID uuid, String name, int playTime) {
		totalTime += playTime;

		// Create new User
		User user = new User(uuid, playTime, false);
		//Put player to the playerMap
		playerMap.put(uuid.toString(), user);
		assert name != null;
		name2uuid.put(name.toLowerCase(), uuid);
	}

	static class User {
		private final UUID uuid;
		private int playTimeTicks;
		private boolean isOnline;

		public User(UUID uuid, int playTimeTicks, boolean isOnline) {
			this.uuid = uuid;
			this.playTimeTicks = playTimeTicks;
			this.isOnline = isOnline;
		}
	}
}
