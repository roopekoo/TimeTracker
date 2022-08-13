package me.roopekoo.timeTracker;

import me.roopekoo.timeTracker.utils.Messages;
import me.roopekoo.timeTracker.utils.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static java.time.temporal.TemporalAdjusters.*;

public class PlayerData {
	private static final String BASEDIR = "plugins/TimeTracker";
	private static final String PATH = "/playerhistory.yml";
	//Store players to memory
	private static final HashMap<String, User> playerMap = new HashMap<>();
	//Main toplist
	private static final ArrayList<User> topTimes = new ArrayList<>();
	private static final ArrayList<User> topDay = new ArrayList<>();
	private static final ArrayList<User> topMonth = new ArrayList<>();
	private static final ArrayList<User> topYear = new ArrayList<>();
	private static final File HISTORY_FILE = new File(BASEDIR+PATH);
	//Store UUIDs in map where username is the key
	private final HashMap<String, UUID> name2uuid = new HashMap<>();
	private final TimeConverter converter = new TimeConverter();
	private final YamlConfiguration HISTORY;
	long updateTotal = 0;
	long updateDay = 0;
	long updateMonth = 0;
	long updateYear = 0;

	long totalTime = 0;
	long totalDay = 0;
	long totalMonth = 0;
	long totalYear = 0;
	// 10-minute topList update delay
	int UPDATEDELAY = 10*60*1000;
	List<String> historySelectors = converter.getTimeHistoryArray();

	public PlayerData() {
		File f = new File(BASEDIR);
		if(!f.exists()) {
			f.mkdir();
		}
		if(!f.exists()) {
			try {
				HISTORY_FILE.createNewFile();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		HISTORY = YamlConfiguration.loadConfiguration(HISTORY_FILE);
	}

	public static int getListSize(String selector) {
		int size = 0;
		switch(selector) {
			case "total":
				size = topTimes.size();
				break;
			case "day":
				size = topDay.size();
				break;
			case "month":
				size = topMonth.size();
				break;
			case "year":
				size = topYear.size();
				break;
		}
		return size;
	}

	public void printTopList(CommandSender sender, String pageNo, String selector, String timeFormat) {
		//check that pageNo is not too big
		int pages = (int) Math.ceil((double) PlayerData.getListSize(selector)/10);
		String pageStr = String.valueOf(pages);
		int page;
		if(pages == 0) {
			sender.sendMessage(Messages.TITLE+Messages.EMPTY_LIST.toString().replace("{0}", selector));
			return;
		}
		if(pageNo.equals("")) {
			page = 1;
		} else {
			page = Integer.parseInt(pageNo);
		}
		if(page>pages) {
			sender.sendMessage(Messages.TITLE+Messages.INVALID_PAGE.toString());
		} else {
			String username;
			String playtime;
			String index;
			Messages isOnline;
			long ticks;
			int userIndex = (page-1)*10+1;
			//Check if list needs refreshing
			if(isTopListOld(selector)) {
				sender.sendMessage(Messages.TITLE+Messages.LIST_UPDATE.toString());
				sortTimes(selector);
			}
			pageNo = String.valueOf(page);
			if(selector.equals("total")) {
				sender.sendMessage(Messages.TITLE+
				                   Messages.TOPLIST_TITLE.toString().replace("{0}", pageNo).replace("{1}", pageStr));
			} else {
				sender.sendMessage(Messages.TITLE+
				                   Messages.TPH_TITLE.toString().replace("{0}", selector).replace("{1}", pageNo)
				                                     .replace("{2}", pageStr));
			}
			//Get correct slice of toplist
			List<PlayerData.User> topListPage = getTopListPage(page, selector);
			for(PlayerData.User user: topListPage) {
				isOnline = Messages.OFFLINE;
				username = user.name;
				if(selector.equals("total")) {
					ticks = getPlaytime(username);
				} else {
					ticks = getResetTime(username, selector);
				}
				if(timeFormat.equals("")) {
					playtime = converter.fullTimeToStr(ticks);
				} else {
					playtime = converter.formatPlaytime(ticks, timeFormat);
				}
				if(user.isOnline) {
					isOnline = Messages.ONLINE;
				}
				index = String.valueOf(userIndex);
				sender.sendMessage(
						Messages.TOPLIST_MAIN.toString().replace("{0}", index).replace("{1}", isOnline.toString())
						                     .replace("{2}", username).replace("{3}", playtime));
				userIndex++;
			}
			if(page != pages) {
				pageNo = String.valueOf(page+1);
				sender.sendMessage(Messages.TOPLIST_FOOTER.toString().replace("{0}", pageNo));
			}
		}
	}

	public String getHistory(String username, String timeHistory, String timeFormat) {
		long playtime;
		if(timeHistory.equals("")) {
			playtime = getPlaytime(username);
		} else {
			playtime = getResetTime(username, timeHistory);
		}
		if(timeFormat.equals("")) {
			return converter.fullTimeToStr(playtime);
		}
		return converter.formatPlaytime(playtime, timeFormat);
	}

	public void initializePlayerData() {
		UUID uuid;
		int playTime;
		int dayR;
		int monthR;
		int yearR;

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
				addNewPlayer(uuid, name, playTime, dayR, monthR, yearR);
			}
		}
		for(String e: historySelectors) {
			checkDate(e);
			sortTimes(e);
		}
		sortTimes("total");
	}

	public void writeFile() {
		ForkJoinPool.commonPool().submit(()->{
			try {
				HISTORY.save(HISTORY_FILE);
			} catch(IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void setTimer(long seconds, String selector) {
		new BukkitRunnable() {
			@Override public void run() {
				updateHistory(selector);
				clearHistoryList(selector);
				//set every player isOnList to false
				offlinePlayersLoop(selector);
				//Insert back users to history array that are online
				onlinePlayersLoop(selector);
			}
		}.runTaskLater(TimeTracker.getPlugin(), seconds*20L);
	}

	void clearHistoryList(String selector) {
		switch(selector) {
			case "day":
				topDay.clear();
				break;
			case "month":
				topMonth.clear();
				break;
			case "year":
				topYear.clear();
				break;
		}
	}

	private void offlinePlayersLoop(String selector) {
		for(Map.Entry<String, User> set: playerMap.entrySet()) {
			forceOnlineMode(selector, set.getValue());
		}
	}

	private void forceOnlineMode(String selector, User user) {
		switch(selector) {
			case "day":
				user.isOnDayList = false;
				break;
			case "month":
				user.isOnMonthList = false;
				break;
			case "year":
				user.isOnYearList = false;
				break;
		}
	}

	private void insertHistory(UUID uuid, String selector) {
		User user = playerMap.get(uuid.toString());
		switch(selector) {
			case "day":
				topDay.add(user);
				user.isOnDayList = true;
				break;
			case "month":
				topMonth.add(user);
				user.isOnMonthList = true;
				break;
			case "year":
				topYear.add(user);
				user.isOnYearList = true;
				break;
		}
	}

	void onlinePlayersLoop(String selector) {
		for(Player player: Bukkit.getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();
			insertHistory(uuid, selector);
		}
	}

	/**
	 Checks if current history selector is in the YML If not, create it
	 */
	private void checkDate(String selection) {
		LocalDate today = LocalDate.now();
		LocalDateTime todayStart = today.atStartOfDay();
		if(noSectionInYML(selection, null)) {
			LocalDateTime nextDate;
			switch(selection) {
				case "day":
					HISTORY.set(selection, todayStart.toString());
					break;
				case "month":
					nextDate = today.with(firstDayOfMonth()).atStartOfDay();
					HISTORY.set(selection, nextDate.toString());
					break;
				case "year":
					nextDate = today.with(firstDayOfYear()).atStartOfDay();
					HISTORY.set(selection, nextDate.toString());
					break;
			}
		}
		updateHistory(selection);
	}

	private void updateHistory(String selection) {
		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		boolean updateYML = false;

		String date = HISTORY.getString(selection);
		assert date != null;
		LocalDateTime oldDate = LocalDateTime.parse(date);

		LocalDateTime nextDate = todayStart;
		LocalDateTime now = LocalDateTime.now();
		//Check if YML reset date is old
		switch(selection) {
			case "day":
				nextDate = todayStart.plusDays(1);
				if(oldDate.getDayOfMonth() != todayStart.getDayOfMonth()) {
					updateYML = true;
				}
				break;
			case "month":
				nextDate = todayStart.with(firstDayOfNextMonth());
				if(oldDate.getMonthValue() != todayStart.getMonthValue()) {
					updateYML = true;
				}
				break;
			case "year":
				nextDate = todayStart.with(firstDayOfNextYear());
				if(oldDate.getYear() != todayStart.getYear()) {
					updateYML = true;
				}
				break;
		}
		if(updateYML) {
			UUID uuid;
			int playTime;
			User user;

			//update date on YML
			HISTORY.set(selection, now.toString());

			OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
			for(OfflinePlayer offlinePlayer: offlinePlayers) {
				uuid = offlinePlayer.getUniqueId();
				playTime = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);
				user = playerMap.get(uuid.toString());
				switch(selection) {
					case "day":
						user.dayReset = playTime;
						break;
					case "month":
						user.monthReset = playTime;
						break;
					case "year":
						user.yearReset = playTime;
						break;
				}
				HISTORY.set("players."+uuid+"."+selection, playTime);
			}
		}
		long diff = Duration.between(now, nextDate).toSeconds();
		setTimer(diff, selection);
		writeFile();
	}

	private boolean noSectionInYML(String section, UUID uuid) {
		if(uuid == null) {
			return HISTORY.getString(section) == null;
		}
		ConfigurationSection sec = HISTORY.getConfigurationSection(section);
		return sec == null || !sec.contains(uuid.toString());
	}

	public void sortTimes(String selector) {
		UUID uuid;
		long time = System.currentTimeMillis();

		switch(selector) {
			case "total":
				updateTotal = time;
				totalTime = 0;
				topTimes.sort(new compTimes());

				for(User user: topTimes) {
					uuid = user.uuid;
					//Playtime is old
					if(user.isOnline) {
						updatePlaytime(uuid);
					}
					totalTime += user.playTimeTicks;
				}
				break;
			case "day":
				updateDay = time;
				totalDay = 0;
				topDay.sort(new compDay());

				for(User user: topDay) {
					uuid = user.uuid;
					//Playtime is old
					if(user.isOnline) {
						updatePlaytime(uuid);
					}
					totalDay += user.playTimeTicks-user.dayReset;
				}
				break;
			case "month":
				updateMonth = time;
				totalMonth = 0;
				topMonth.sort(new compMonth());

				for(User user: topMonth) {
					uuid = user.uuid;
					//Playtime is old
					if(user.isOnline) {
						updatePlaytime(uuid);
					}
					totalMonth += user.playTimeTicks-user.monthReset;
				}
				break;
			case "year":
				updateYear = time;
				totalYear = 0;
				topYear.sort(new compYear());

				for(User user: topYear) {
					uuid = user.uuid;
					//Playtime is old
					if(user.isOnline) {
						updatePlaytime(uuid);
					}
					totalYear += user.playTimeTicks-user.yearReset;
				}
				break;
		}
	}

	public List<User> getTopListPage(int page, String selector) {
		int high = page*10;
		int size = getListSize(selector);
		List<User> list = null;
		if(high>size) {
			high = size;
		}
		int low = (page-1)*10;
		switch(selector) {
			case "total":
				list = topTimes.subList(low, high);
				break;
			case "day":
				list = topDay.subList(low, high);
				break;
			case "month":
				list = topMonth.subList(low, high);
				break;
			case "year":
				list = topYear.subList(low, high);
				break;
		}
		return list;
	}

	public boolean isUserValid(String arg) {
		if(arg.equals("total")) {
			return true;
		}
		return name2uuid.containsKey(arg.toLowerCase());
	}

	public long getPlaytime(String username) {
		if(username.equalsIgnoreCase("total")) {
			if(isTopListOld(username)) {
				sortTimes(username);
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

	public void addNewPlayer(UUID uuid, String name, int playTime, int dayR, int monthR, int yearR) {
		totalTime += playTime;

		// Create new User
		User user = new User(uuid, name, playTime, dayR, monthR, yearR);
		//Put player to the playerMap
		playerMap.put(uuid.toString(), user);
		assert name != null;
		name2uuid.put(name.toLowerCase(), uuid);
		topTimes.add(user);
		if(user.playTimeTicks-user.dayReset>0) {
			topDay.add(user);
			user.isOnDayList = true;
		}
		if(user.playTimeTicks-user.monthReset>0) {
			topMonth.add(user);
			user.isOnMonthList = true;
		}
		if(user.playTimeTicks-user.yearReset>0) {
			topYear.add(user);
			user.isOnYearList = true;
		}
	}

	public boolean isTopListOld(String selector) {
		boolean isOld = false;
		switch(selector) {
			case "total":
				isOld = System.currentTimeMillis()-updateTotal>UPDATEDELAY;
				break;
			case "day":
				isOld = System.currentTimeMillis()-updateDay>UPDATEDELAY;
				break;
			case "month":
				isOld = System.currentTimeMillis()-updateMonth>UPDATEDELAY;
				break;
			case "year":
				isOld = System.currentTimeMillis()-updateYear>UPDATEDELAY;
				break;
		}
		return isOld;
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
		if(username.equalsIgnoreCase("total")) {
			if(isTopListOld(timeHistory)) {
				sortTimes(timeHistory);
			}
			long total = 0;
			//return correct total
			switch(timeHistory) {
				case "day":
					total = totalDay;
					break;
				case "month":
					total = totalMonth;
					break;
				case "year":
					total = totalYear;
					break;
			}
			return total;
		}

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

	public void historyListCheck(UUID uuid) {
		User user = playerMap.get(uuid.toString());
		if(!user.isOnDayList) {
			topDay.add(user);
			user.isOnDayList = true;
		}
		if(!user.isOnMonthList) {
			topMonth.add(user);
			user.isOnMonthList = true;
		}
		if(!user.isOnYearList) {
			topYear.add(user);
			user.isOnYearList = true;
		}
	}

	static class compTimes implements Comparator<User> {
		@Override public int compare(User o1, User o2) {
			return Integer.compare(o2.playTimeTicks, o1.playTimeTicks);
		}
	}

	static class compDay implements Comparator<User> {
		@Override public int compare(User o1, User o2) {
			return Integer.compare(o2.playTimeTicks-o2.dayReset, o1.playTimeTicks-o1.dayReset);
		}
	}

	static class compMonth implements Comparator<User> {
		@Override public int compare(User o1, User o2) {
			return Integer.compare(o2.playTimeTicks-o2.monthReset, o1.playTimeTicks-o1.monthReset);
		}
	}

	static class compYear implements Comparator<User> {
		@Override public int compare(User o1, User o2) {
			return Integer.compare(o2.playTimeTicks-o2.yearReset, o1.playTimeTicks-o1.yearReset);
		}
	}

	public static class User {
		String name;
		boolean isOnline = false;
		boolean isOnDayList = false;
		boolean isOnMonthList = false;
		boolean isOnYearList = false;
		UUID uuid;
		int playTimeTicks;
		int dayReset;
		int monthReset;
		int yearReset;


		public User(UUID uuid, String name, int playTimeTicks, int dayReset, int monthReset, int yearReset) {
			this.uuid = uuid;
			this.name = name;
			this.playTimeTicks = playTimeTicks;
			this.dayReset = dayReset;
			this.monthReset = monthReset;
			this.yearReset = yearReset;
		}
	}
}
