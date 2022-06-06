package me.roopekoo.toptime;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.*;

public class PlayerData {
	//Store players to memory
	private static final HashMap<String, User> playerMap = new HashMap<>();
	//Main toplist
	private static final ArrayList<User> topTimes = new ArrayList<>();
	//Store UUIDs in map where username is the key
	private final HashMap<String, UUID> name2uuid = new HashMap<>();
	long updateTime = 0;
	long totalTime = 0;
	// 10-minute topList update delay
	int UPDATEDELAY = 10*60*1000;

	public static int getListSize() {
		return topTimes.size();
	}

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
			addNewPlayer(uuid, name, playTime, false);

		}
		sortTimes();
	}

	public void sortTimes() {
		updateTime = System.currentTimeMillis();
		topTimes.sort(new compTimes());
	}

	public List<User> getTopListPage(int page) {
		int high = page*10;
		int size = getListSize();
		if(high>size) {
			high = size;
		}
		int low = (page-1)*10;
		return topTimes.subList(low, high);
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

	public void addNewPlayer(UUID uuid, String name, int playTime, boolean isOnline) {
		totalTime += playTime;

		// Create new User
		User user = new User(uuid, name, playTime, isOnline);
		//Put player to the playerMap
		playerMap.put(uuid.toString(), user);
		assert name != null;
		name2uuid.put(name.toLowerCase(), uuid);
		topTimes.add(user);
	}

	public boolean isTopListOld() {
		return System.currentTimeMillis()-updateTime>UPDATEDELAY;
	}

	static class compTimes implements Comparator<User> {
		@Override public int compare(User o1, User o2) {
			return Integer.compare(o2.playTimeTicks, o1.playTimeTicks);
		}
	}

	static class User {
		private final UUID uuid;
		String name;
		int playTimeTicks;
		private boolean isOnline;

		public User(UUID uuid, String name, int playTimeTicks, boolean isOnline) {
			this.uuid = uuid;
			this.name = name;
			this.playTimeTicks = playTimeTicks;
			this.isOnline = isOnline;
		}
	}
}
