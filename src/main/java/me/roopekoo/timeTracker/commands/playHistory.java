package me.roopekoo.timeTracker.commands;

import me.roopekoo.timeTracker.PlayerData;
import me.roopekoo.timeTracker.TimeTracker;
import me.roopekoo.timeTracker.utils.Messages;
import me.roopekoo.timeTracker.utils.TimeConverter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class playHistory implements CommandExecutor {
	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();
	TimeConverter converter = new TimeConverter();

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("timetracker.playhistory")) {
			sender.sendMessage(Messages.TITLE+Messages.NO_PERM.toString());
			return true;
		}
		switch(args.length) {
			case 0:
				sender.sendMessage(Messages.TITLE+Messages.NOT_ENOUGH_PARAMS.toString());
				return false;
			case 1:
				if(converter.isTimeHistory(args[0])) {
					if(sender instanceof Player) {
						//playhistory [day/month/year]
						printPlaytimeHistory(sender, args[0], sender.getName(), "");
						return true;
					} else {
						sender.sendMessage(Messages.TITLE+Messages.PLAYER_REQUIRED.toString());
						return false;
					}
				} else {
					sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
					return false;
				}
			case 2:
				if(playerData.isUserValid(args[0])) {
					if(converter.isTimeHistory(args[1])) {
						//playhistory player [day/month/year]
						printPlaytimeHistory(sender, args[1], args[0], "");
						return true;
					} else {
						sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
						return false;
					}
				} else if(converter.isTimeHistory(args[0])) {
					if(sender instanceof Player) {
						if(converter.isTimeFormat(args[1])) {
							//playhistory [day/month/year] [timeFormat]
							printPlaytimeHistory(sender, args[0], sender.getName(), args[1]);
							return true;
						} else {
							sender.sendMessage(Messages.TITLE+Messages.INVALID_TIME_FORMAT.toString());
							return false;
						}
					} else {
						sender.sendMessage(Messages.TITLE+Messages.PLAYER_REQUIRED.toString());
						return false;
					}
				} else {
					sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
					return false;
				}
			case 3:
				if(playerData.isUserValid(args[0])) {
					if(converter.isTimeHistory(args[1])) {
						if(converter.isTimeFormat(args[2])) {
							//playhistory <player> [day/month/year] [timeFormat]
							printPlaytimeHistory(sender, args[1], args[0], args[2]);
							return true;
						} else {
							sender.sendMessage(Messages.TITLE+Messages.INVALID_TIME_FORMAT.toString());
							return false;
						}
					} else {
						sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
						return false;
					}
				} else {
					sender.sendMessage(Messages.TITLE+Messages.INVALID_USERNAME.toString());
					return false;
				}
			default:
				sender.sendMessage(Messages.TITLE+Messages.TOO_MANY_PARAMS.toString());
				return false;
		}
	}

	private void printPlaytimeHistory(CommandSender sender, String timeHistory, String username, String timeFormat) {
		String resettime = playerData.getHistory(username, timeHistory, timeFormat);
		Messages timeHistoryMsg = Messages.fromString(timeHistory);
		assert timeHistoryMsg != null;
		//Fix name formatting
		username = playerData.getNameFormat(username);
		if(sender.getName().equalsIgnoreCase(username)) {
			sender.sendMessage(Messages.TITLE+Messages.HISTORY_SELF.toString().replace("{0}",
			                                                                           timeHistoryMsg.toString())
			                                                       .replace("{1}", resettime));
		} else {
			Messages isOnline = Messages.OFFLINE;
			if(playerData.isOnline(username)) {
				isOnline = Messages.ONLINE;
			}
			sender.sendMessage(Messages.TITLE+
			                   Messages.HISTORY.toString().replace("{0}", isOnline.toString()).replace("{1}", username)
			                                   .replace("{2}", resettime).replace("{3}", timeHistoryMsg.toString()));
		}
	}
}
