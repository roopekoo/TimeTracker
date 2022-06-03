package me.roopekoo.toptime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

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
		if(ticks/TicksToUnit.SECOND.value>1) {
			value = (int) (ticks/TicksToUnit.SECOND.value);
			s = s+value+"s";
			ticks = (long) (ticks-value*TicksToUnit.SECOND.value);
		}
		if(ticks != 0) {
			value = (int) (ticks/TicksToUnit.MILLISECOND.value);
			s = s+value+"ms";
		}
		return s;
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
		String formattedTime = String.format("%.2f", playtime/unit);
		return formattedTime+format;
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
