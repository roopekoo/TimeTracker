package me.roopekoo.timeTracker.commands;

import me.roopekoo.timeTracker.PlayerData;
import me.roopekoo.timeTracker.TimeTracker;
import me.roopekoo.timeTracker.utils.Messages;
import me.roopekoo.timeTracker.utils.TimeConverter;
import me.roopekoo.timeTracker.utils.Utilities;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class topPlayHistory implements CommandExecutor {
	PlayerData playerData = TimeTracker.getPlugin().getPlayerData();
	TimeConverter converter = new TimeConverter();
	Utilities util = new Utilities();

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("timetracker.topplayhistory")) {
			sender.sendMessage(Messages.TITLE+Messages.NO_PERM.toString());
			return true;
		}
		if(args.length>0) {
			if(converter.isTimeHistory(args[0])) {
				if(args.length == 1) {
					//tph <day/month/year>
					playerData.printTopList(sender, "", args[0], "");
					return true;
				} else if(args.length == 2) {
					if(util.isPositiveInteger(args[1])) {
						//tph <day/month/year> page
						playerData.printTopList(sender, args[1], args[0], "");
						return true;
					} else if(converter.isTimeFormat(args[1])) {
						//tph  <day/month/year> [timeFormat]
						playerData.printTopList(sender, "", args[0], args[1]);
						return true;
					} else {
						sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
						return false;
					}
				} else if(args.length == 3) {
					if(util.isPositiveInteger(args[1])) {
						if(converter.isTimeFormat(args[2])) {
							//tph <day/month/year> page [timeFormat]
							playerData.printTopList(sender, args[1], args[0], args[2]);
							return true;
						} else {
							sender.sendMessage(Messages.TITLE+Messages.INVALID_TIME_FORMAT.toString());
							return false;
						}
					} else {
						sender.sendMessage(Messages.TITLE+Messages.INVALID_PAGE_NO.toString());
						return false;
					}
				} else {
					sender.sendMessage(Messages.TITLE+Messages.TOO_MANY_PARAMS.toString());
					return false;
				}
			} else {
				sender.sendMessage(Messages.TITLE+Messages.INVALID_PARAM.toString());
				return false;
			}
		} else {
			sender.sendMessage(Messages.TITLE+Messages.NOT_ENOUGH_PARAMS.toString());
			return false;
		}
	}
}