package me.roopekoo.timeTracker;

import me.roopekoo.timeTracker.utils.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PlayerData {
	//Store players to memory
	private static final HashMap<String, User> playerMap = new HashMap<>();
	//Main toplist
	private static final ArrayList<User> topTimes = new ArrayList<>();
	private static File HISTORY_FILE;
	//Store UUIDs in map where username is the key
	private final HashMap<String, UUID> name2uuid = new HashMap<>();
	TimeConverter converter = new TimeConverter();
	long updateTime = 0;
	long totalTime = 0;
	// 10-minute topList update delay
	int UPDATEDELAY = 10*60*1000;
	List<String> historySelectors = converter.getTimeHistoryArray();
	private YamlConfiguration HISTORY;

	public static int getListSize() {
		return topTimes.size();
	}

	public void initializePlayerData() {
		UUID uuid;
		int playTime;
		int dayR;
		int monthR;
		int yearR;

		HISTORY_FILE = TimeTracker.getPlugin().createFile("playerhistory.yml");
		HISTORY = YamlConfiguration.loadConfiguration(HISTORY_FILE);

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
		// Go through all offline players
		for(OfflinePlayer offlinePlayer: offlinePlayers) {
			uuid = offlinePlayer.getUniqueId();
			String name = offlinePlayer.getName();
			playTime = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);

			if(name != null) {
				if(noSectionInYML("players", uuid)) {
					//Set reset time to amount of playtime on the server
					HISTORY.set("players."+uuid+".day", playTime);
					HISTORY.set("players."+uuid+".month", playTime);
					HISTORY.set("players."+uuid+".year", playTime);
				}
				dayR = HISTORY.getInt("players."+uuid+".day");
				monthR = HISTORY.getInt("players."+uuid+".month");
				yearR = HISTORY.getInt("players."+uuid+".year");
				addNewPlayer(uuid, name, playTime, false, dayR, monthR, yearR);
			}
		}
		for(String e: historySelectors) {
			checkDate(e);
		}
		sortTimes();
	}

	private void setTimer(long seconds, String selection) {

		new BukkitRunnable() {
			@Override public void run() {
				LocalDate now = LocalDate.now();
				LocalDateTime todayStart = now.atStartOfDay();
				if(selection.equals("day")) {
					LocalDateTime tomorrow = now.plusDays(1).atStartOfDay();
					updateHistory(selection, todayStart, now, tomorrow);
				}
				if(selection.equals("month")) {
					LocalDateTime nextMonth = now.plusMonths(1).atStartOfDay();
					updateHistory(selection, todayStart, now, nextMonth);
				}
			}
		}.runTaskLaterAsynchronously(TimeTracker.getPlugin(), seconds*20L);
	}

	private void checkDate(String selection) {
		LocalDate now = LocalDate.now();
		LocalDateTime todayStart = now.atStartOfDay();
		if(noSectionInYML(selection, null)) {
			HISTORY.set(selection, todayStart.toString());
		} else {
			String date = HISTORY.getString(selection);
			assert date != null;
			LocalDateTime oldDate = LocalDateTime.parse(date);
			if(selection.equals("day")) {
				if(oldDate.getDayOfMonth() != todayStart.getDayOfMonth()) {
					LocalDateTime tomorrow = now.plusDays(1).atStartOfDay();
					updateHistory(selection, todayStart, now, tomorrow);
				}
			}
			if(selection.equals("month")) {
				if(oldDate.getMonthValue() != todayStart.getMonthValue()) {
					LocalDateTime nextMonth = now.plusMonths(1).atStartOfDay();
					updateHistory(selection, todayStart, now, nextMonth);
				}
			}
		}
	}

	private void updateHistory(String selection, LocalDateTime today, LocalDate now, LocalDateTime nextDate) {
		UUID uuid;
		int playTime;

		HISTORY.set(selection, today.toString());
		long diff = ChronoUnit.SECONDS.between(now, nextDate);
		setTimer(diff, selection);

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		for(OfflinePlayer offlinePlayer: offlinePlayers) {
			uuid = offlinePlayer.getUniqueId();
			playTime = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);
			HISTORY.set("players."+uuid+"."+selection, playTime);
		}
	}

	private boolean noSectionInYML(String section, UUID uuid) {
		ConfigurationSection sec = HISTORY.getConfigurationSection(section);
		if(uuid == null) {
			return sec == null;
		}
		return sec == null || !sec.contains(uuid.toString());
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

	public void addNewPlayer(UUID uuid, String name, int playTime, boolean isOnline, int dayR, int monthR, int yearR) {
		totalTime += playTime;

		// Create new User
		User user = new User(uuid, name, playTime, isOnline, dayR, monthR, yearR);
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

	public long getResetTime(String username, String timeHistory) {
		UUID uuid = name2uuid.get(username.toLowerCase());
		User user = playerMap.get(uuid.toString());
		int resettime = 0;

		switch(timeHistory) {
			case "day":
				resettime = user.dayReset;
				break;
			case "month":
				resettime = user.monthReset;
				break;
			case "year":
				resettime = user.yearReset;
				break;
		}
		if(user.isOnline) {
			int ticks = Bukkit.getOfflinePlayer(user.uuid).getStatistic(Statistic.PLAY_ONE_MINUTE);
			user.playTimeTicks = ticks;
			return ticks-resettime;
		}
		return user.playTimeTicks-resettime;
	}

	static class compTimes implements Comparator<User> {
		@Override public int compare(User o1, User o2) {
			return Integer.compare(o2.playTimeTicks, o1.playTimeTicks);
		}
	}

	public static class User {
		public String name;
		public boolean isOnline;
		UUID uuid;
		int playTimeTicks;
		int dayReset;
		int monthReset;
		int yearReset;

		public User(UUID uuid, String name, int playTimeTicks, boolean isOnline, int dayReset, int monthReset,
		            int yearReset) {
			this.uuid = uuid;
			this.name = name;
			this.playTimeTicks = playTimeTicks;
			this.isOnline = isOnline;
			this.dayReset = dayReset;
			this.monthReset = monthReset;
			this.yearReset = yearReset;
		}
	}
}
