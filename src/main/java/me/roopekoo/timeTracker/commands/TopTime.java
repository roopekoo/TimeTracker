package me.roopekoo.timeTracker.commands;

import me.roopekoo.timeTracker.PlayerData;
import me.roopekoo.timeTracker.TimeTracker;
import me.roopekoo.timeTracker.utils.Messages;
import me.roopekoo.timeTracker.utils.TimeConverter;
import me.roopekoo.timeTracker.utils.Utilities;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TopTime implements CommandExecutor {
	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();
	TimeConverter converter = new TimeConverter();
	Utilities util = new Utilities();

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String selector = "total";
		if(!sender.hasPermission("timetracker.toptime")) {
			sender.sendMessage(Messages.TITLE+Messages.NO_PERM.toString());
			return true;
		}
		if(args.length>2) {
			sender.sendMessage(Messages.TITLE+Messages.TOO_MANY_PARAMS.toString());
			return false;
		}
		if(args.length == 2) {
			//check arg1 is number
			if(util.isPositiveInteger(args[0])) {
				//Check if timeformat is correct
				if(converter.isTimeFormat(args[1])) {
					//toptime page format
					playerData.printTopList(sender, args[0], selector, args[1]);
					return true;
				} else {
					sender.sendMessage(Messages.TITLE+Messages.INVALID_TIME_FORMAT.toString());
				}
			} else {
				sender.sendMessage(Messages.TITLE+Messages.INVALID_PAGE_NO.toString());
			}
			return false;
		}
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("force")) {
				if(!sender.hasPermission("timetracker.timeTracker.force")) {
					//toptime force
					sender.sendMessage(Messages.TITLE+Messages.NO_PERM.toString());
					return true;
				}
				playerData.sortTimes("total");
				sender.sendMessage(Messages.TITLE+Messages.FORCE_UPDATED.toString());
				return true;
			}
			//check if arg 1 is valid number
			if(util.isPositiveInteger(args[0])) {
				//toptime page
				playerData.printTopList(sender, args[0], selector, "");
				return true;
			}
			//check if arg 1 is valid timeFormat string
			if(converter.isTimeFormat(args[0])) {
				//toptime format
				playerData.printTopList(sender, "", selector, args[0]);
				return true;
			}
			sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
			return false;
		}
		playerData.printTopList(sender, "", selector, "");
		return true;
	}
}
