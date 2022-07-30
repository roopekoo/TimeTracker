package me.roopekoo.timeTracker.utils;

import org.bukkit.command.CommandSender;

import java.util.*;

public class TimeConverter {
	private static final HashMap<String, TicksToUnit> TimeFormats;
	private static final HashMap<String, String> longUnit2Short;

	static {
		TimeFormats = new HashMap<>();
		TimeFormats.put("y", TicksToUnit.YEAR);
		TimeFormats.put("year", TicksToUnit.YEAR);
		TimeFormats.put("mo", TicksToUnit.MONTH);
		TimeFormats.put("month", TicksToUnit.MONTH);
		TimeFormats.put("d", TicksToUnit.DAY);
		TimeFormats.put("day", TicksToUnit.DAY);
		TimeFormats.put("h", TicksToUnit.HOUR);
		TimeFormats.put("hour", TicksToUnit.HOUR);
		TimeFormats.put("m", TicksToUnit.MINUTE);
		TimeFormats.put("minute", TicksToUnit.MINUTE);
		TimeFormats.put("s", TicksToUnit.SECOND);
		TimeFormats.put("second", TicksToUnit.SECOND);
		TimeFormats.put("ms", TicksToUnit.MILLISECOND);
		TimeFormats.put("millisecond", TicksToUnit.MILLISECOND);
		TimeFormats.put("t", TicksToUnit.TICK);
		TimeFormats.put("tick", TicksToUnit.TICK);
	}

	static {
		longUnit2Short = new HashMap<>();
		longUnit2Short.put("year", "y");
		longUnit2Short.put("month", "mo");
		longUnit2Short.put("day", "d");
		longUnit2Short.put("hour", "h");
		longUnit2Short.put("minute", "m");
		longUnit2Short.put("second", "s");
		longUnit2Short.put("millisecond", "ms");
		longUnit2Short.put("tick", "t");
	}

	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();

	public ArrayList<String> getTimeFormatsArray() {
		Set<String> formatSet = TimeFormats.keySet();
		return new ArrayList<>(formatSet);
	}

	private String fullTimeToStr(long ticks) {
		int value;
		String s = "";
		if(ticks/TicksToUnit.YEAR.value>1) {
			value = (int) (ticks/TicksToUnit.YEAR.value);
			s = value+"y";
			ticks = (long) (ticks-value*TicksToUnit.YEAR.value);
		}
		if(ticks/TicksToUnit.MONTH.value>1) {
			value = (int) (ticks/TicksToUnit.MONTH.value);
			s = s+value+"Mo";
			ticks = (long) (ticks-value*TicksToUnit.MONTH.value);
		}
		if(ticks/TicksToUnit.DAY.value>1) {
			value = (int) (ticks/TicksToUnit.DAY.value);
			s = s+value+"d";
			ticks = (long) (ticks-value*TicksToUnit.DAY.value);
		}
		if(ticks/TicksToUnit.HOUR.value>1) {
			value = (int) (ticks/TicksToUnit.HOUR.value);
			s = s+value+"h";
			ticks = (long) (ticks-value*TicksToUnit.HOUR.value);
		}
		if(ticks/TicksToUnit.MINUTE.value>1) {
			value = (int) (ticks/TicksToUnit.MINUTE.value);
			s = s+value+"m";
			ticks = (long) (ticks-value*TicksToUnit.MINUTE.value);
		}
		return s+ticks/TicksToUnit.SECOND.value+"s";
	}

	public boolean isTimeFormat(String value) {
		return TimeFormats.containsKey(value);
	}

	public String getPlaytime(String username, String timeFormat) {
		long playtime = playerData.getPlaytime(username);
		if(Objects.equals(timeFormat, "")) {
			return fullTimeToStr(playtime);
		}
		return formatPlaytime(playtime, timeFormat);
	}

	private String formatPlaytime(long playtime, String timeFormat) {
		double unit = TimeFormats.get(timeFormat).value;
		String format = timeFormat;
		if(longUnit2Short.containsKey(timeFormat)) {
			format = longUnit2Short.get(timeFormat);
		}
		if(format.equals("ms") || format.equals("t")) {
			return String.format("%,.0f", playtime/unit)+format;
		}
		return String.format("%,.2f", playtime/unit)+format;
	}

	public void printTopList(CommandSender sender, String pageNo, String timeFormat) {
		//check that pageNo is not too big
		int pages = (int) Math.ceil((double) PlayerData.getListSize()/10);
		String pageStr = String.valueOf(pages);
		int page;
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
			if(playerData.isTopListOld()) {
				sender.sendMessage(Messages.TITLE+Messages.LIST_UPDATE.toString());
				playerData.sortTimes();
			}
			pageNo = String.valueOf(page);
			sender.sendMessage(
					Messages.TITLE+Messages.TOPLIST_TITLE.toString().replace("{0}", pageNo).replace("{1}", pageStr));
			//Get correct slice of toplist
			List<PlayerData.User> topListPage = playerData.getTopListPage(page);
			for(PlayerData.User user: topListPage) {
				isOnline = Messages.OFFLINE;
				username = user.name;
				ticks = playerData.getPlaytime(username);
				if(timeFormat.equals("")) {
					playtime = fullTimeToStr(ticks);
				} else {
					playtime = formatPlaytime(ticks, timeFormat);
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

	private enum TicksToUnit {
		TICK(1),
		MILLISECOND(0.02),
		SECOND(20),
		MINUTE(20*60),
		HOUR(20*60*60),
		DAY(20*60*60*24),
		MONTH(20*60*60*2*365.25),
		YEAR(20*60*60*24*365.25);

		public final double value;

		/**
		 Construct value
		 @param value
		 double
		 */
		TicksToUnit(double value) {
			this.value = value;
		}
	}
}
