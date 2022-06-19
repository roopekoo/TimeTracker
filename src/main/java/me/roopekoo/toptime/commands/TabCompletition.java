package me.roopekoo.toptime.commands;

import me.roopekoo.toptime.PlayerData;
import me.roopekoo.toptime.TimeConverter;
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
			}
			if(args.length == 2) {
				return converter.getTimeFormatsArray();
			}
		}
		if(command.getName().equalsIgnoreCase("toptime")) {
			if(args.length == 1) {
				List<String> list = new ArrayList<>();
				int pages = (int) Math.ceil((double) PlayerData.getListSize()/10);
				for(int i = 0; i<pages; i++) {
					list.add(String.valueOf(i+1));
				}
				return list;
			}
			if(args.length == 2) {
				return converter.getTimeFormatsArray();
			}
		}
		return null;
	}
}
