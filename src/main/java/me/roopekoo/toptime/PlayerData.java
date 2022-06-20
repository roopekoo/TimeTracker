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

			if(name != null) {
				addNewPlayer(uuid, name, playTime, false);
			}

		}
		sortTimes();
	}

	public void sortTimes() {
		User user;
		UUID uuid;
		updateTime = System.currentTimeMillis();
		totalTime = 0;
		// Update array to most recent playerData
		for(User topTime: topTimes) {
			user = topTime;
			uuid = user.uuid;
			//Playtime is old
			if(user.isOnline) {
				updatePlaytime(uuid);
			}
			totalTime += user.playTimeTicks;
		}
		//sort toplist
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
			if(isTopListOld()) {
				sortTimes();
			}
			return totalTime;
		}
		UUID uuid = name2uuid.get(username.toLowerCase());
		User user = playerMap.get(uuid.toString());
		if(user.isOnline) {
			int ticks = Bukkit.getOfflinePlayer(user.uuid).getStatistic(Statistic.PLAY_ONE_MINUTE);
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

	public boolean isOnline(String username) {
		UUID uuid = name2uuid.get(username.toLowerCase());
		User user = playerMap.get(uuid.toString());
		return user.isOnline;
	}

	public void updatePlaytime(UUID uuid) {
		playerMap.get(uuid.toString()).playTimeTicks =
				Bukkit.getOfflinePlayer(uuid).getStatistic(Statistic.PLAY_ONE_MINUTE);
	}

	public String getNameFormat(String arg) {
		UUID uuid = name2uuid.get(arg.toLowerCase());
		return playerMap.get(uuid.toString()).name;
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
		boolean isOnline;

		public User(UUID uuid, String name, int playTimeTicks, boolean isOnline) {
			this.uuid = uuid;
			this.name = name;
			this.playTimeTicks = playTimeTicks;
			this.isOnline = isOnline;
		}
	}
}
