package me.roopekoo.timeTracker.commands;

import me.roopekoo.timeTracker.Messages;
import me.roopekoo.timeTracker.PlayerData;
import me.roopekoo.timeTracker.TimeConverter;
import me.roopekoo.timeTracker.TimeTracker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class gettime implements CommandExecutor {
	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();
	TimeConverter converter = new TimeConverter();

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("timetracker.gettime")) {
			sender.sendMessage(Messages.TITLE+Messages.NO_PERM.toString());
			return true;
		}
		if(args.length>2) {
			sender.sendMessage(Messages.TITLE+Messages.TOO_MANY_PARAMS.toString());
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
					sender.sendMessage(Messages.TITLE+Messages.INVALID_TIME_FORMAT.toString());
					return false;
				}
			}
			//check if username is valid, arg1
			if(playerData.isUserValid(args[0])) {
				//Check if timeformat is correct
				if(converter.isTimeFormat(args[1])) {
					String user = playerData.getNameFormat(args[0]);
					printPlaytime(sender, user, args[1]);
					return true;
				} else {
					sender.sendMessage(Messages.TITLE+Messages.INVALID_TIME_FORMAT.toString());
				}
			} else {
				sender.sendMessage(Messages.TITLE+Messages.INVALID_USERNAME.toString());
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
					sender.sendMessage(Messages.TITLE+Messages.PLAYER_REQUIRED.toString());
					return false;
				}
				printPlaytime(sender, sender.getName(), args[0]);
				return true;
			}
			sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
			return false;
		}
		if(!(sender instanceof Player)) {
			sender.sendMessage(Messages.TITLE+Messages.PLAYER_REQUIRED.toString());
			return false;
		}
		printPlaytime(sender, sender.getName(), "");
		return true;
	}

	private void printPlaytime(CommandSender sender, String username, String timeFormat) {
		String playtime = converter.getPlaytime(username, timeFormat);
		if(username.equalsIgnoreCase("total")) {
			sender.sendMessage(Messages.TITLE+Messages.GETTIME_TOTAL.toString().replace("{0}", playtime));
		} else {
			if(sender.getName().equalsIgnoreCase(username)) {
				sender.sendMessage(Messages.TITLE+Messages.GETTIME_SELF.toString().replace("{0}", playtime));
			} else {
				Messages isOnline = Messages.OFFLINE;
				if(playerData.isOnline(username)) {
					isOnline = Messages.ONLINE;
				}
				sender.sendMessage(Messages.TITLE+Messages.GETTIME.toString().replace("{0}", isOnline.toString())
				                                                  .replace("{1}", username).replace("{2}", playtime));
			}
		}
	}
}
