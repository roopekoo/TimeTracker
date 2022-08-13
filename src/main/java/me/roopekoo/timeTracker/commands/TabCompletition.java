package me.roopekoo.timeTracker.commands;

import me.roopekoo.timeTracker.PlayerData;
import me.roopekoo.timeTracker.utils.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TabCompletition implements TabCompleter {
	TimeConverter converter = new TimeConverter();

	@Override public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("gettime")) {
			if(args.length == 1) {
				List<String> list = new ArrayList<>();
				list.add("total");
				for(Player p: Bukkit.getOnlinePlayers()) {
					list.add(p.getName());
				}
				return list;
			} else if(args.length == 2) {
				return converter.getTimeFormatsArray();
			}
		}
		if(command.getName().equalsIgnoreCase("toptime")) {
			if(args.length == 1) {
				return getNumberArray("total");
			} else if(args.length == 2) {
				return converter.getTimeFormatsArray();
			}
		}
		if(command.getName().equalsIgnoreCase("playhistory")) {
			if(args.length == 1) {
				List<String> list = converter.getTimeHistoryArray();
				list.add("total");
				for(Player p: Bukkit.getOnlinePlayers()) {
					list.add(p.getName());
				}
				return list;
			} else if(args.length == 2) {
				if(converter.isTimeHistory(args[0])) {
					return converter.getTimeFormatsArray();
				} else {
					return converter.getTimeHistoryArray();
				}
			} else if(args.length == 3) {
				if(converter.isTimeHistory(args[1])) {
					return converter.getTimeFormatsArray();
				}
			}
		}
		if(command.getName().equalsIgnoreCase("topplayhistory")) {
			if(args.length == 1) {
				return converter.getTimeHistoryArray();
			}
			if(args.length == 2) {
				if(converter.isTimeHistory(args[0])) {
					return getNumberArray(args[0]);
				}
			}
			if(args.length == 3) {
				return converter.getTimeFormatsArray();
			}
		}
		return null;
	}

	private List<String> getNumberArray(String selector) {
		int pages = (int) Math.ceil((double) PlayerData.getListSize(selector)/10);
		List<String> list = new ArrayList<>();
		for(int i = 0; i<pages; i++) {
			list.add(String.valueOf(i+1));
		}
		return list;
	}
}
