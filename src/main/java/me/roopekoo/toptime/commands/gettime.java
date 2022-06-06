package me.roopekoo.toptime.commands;

import me.roopekoo.toptime.PlayerData;
import me.roopekoo.toptime.TimeConverter;
import me.roopekoo.toptime.TimeTracker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class gettime implements CommandExecutor {
	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();
	TimeConverter converter = new TimeConverter();

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("timetracker.gettime"))
		{
			sender.sendMessage("You do not have permission to do that");
			return true;
		}
		if(args.length>2) {
			sender.sendMessage("Too many arguments!");
			return false;
		}
		if(args.length == 2) {
			//Check if 1st arg is "total"
			if(args[0].equalsIgnoreCase("total")) {
				//Check if timeformat is correct
				if(converter.isTimeFormat(args[1])) {
					printPlaytime(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage("Invalid time format!");
				}
			}
			//check if username is valid, arg1
			if(playerData.isUserValid(args[0])) {
				//Check if timeformat is correct
				if(converter.isTimeFormat(args[1])) {
					printPlaytime(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage("Invalid time format!");
				}
			} else {
				sender.sendMessage("Invalid username!");
			}
			return false;
		}
		if(args.length == 1) {
			//Check if 1st arg is "total"
			if(args[0].equalsIgnoreCase("total")) {
				printPlaytime(sender, args[0], "");
				return true;
			}
			//check if arg 1 is valid player
			if(playerData.isUserValid(args[0])) {
				printPlaytime(sender, args[0], "");
				return true;
			}
			//check if arg 1 is valid timeFormat string
			if(converter.isTimeFormat(args[0])) {
				if(!(sender instanceof Player)) {
					sender.sendMessage("Playername required!");
					return false;
				}
				printPlaytime(sender, sender.getName(), args[0]);
				return true;
			}
			sender.sendMessage("Invalid parameter!");
			return false;
		}
		if(!(sender instanceof Player)) {
			sender.sendMessage("Playername required!");
			return false;
		}
		printPlaytime(sender, sender.getName(), "");
		return true;
	}

	private void printPlaytime(CommandSender sender, String username, String timeFormat) {
		String playtime = converter.getPlaytime(username, timeFormat);
		if(username.equalsIgnoreCase("total")) {
			sender.sendMessage("All players combined have a playtime of "+playtime);
		} else {
			sender.sendMessage(username+" has a playtime of "+playtime);
		}
	}
}
