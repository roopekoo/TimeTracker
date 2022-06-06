package me.roopekoo.toptime.commands;

import me.roopekoo.toptime.PlayerData;
import me.roopekoo.toptime.TimeConverter;
import me.roopekoo.toptime.TimeTracker;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TopTime implements CommandExecutor {
	TimeConverter converter = new TimeConverter();

	@Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length>2) {
			sender.sendMessage("Too many arguments!");
			return false;
		}
		if(args.length == 2) {
			//check arg1 is number
			if(isPositiveInteger(args[0])) {
				//Check if timeformat is correct
				if(converter.isTimeFormat(args[1])) {
					converter.printTopList(sender, args[0], args[1]);
					return true;
				} else {
					sender.sendMessage("Invalid time format!");
				}
			} else {
				sender.sendMessage("Invalid page number!");
			}
			return false;
		}
		if(args.length == 1) {
			//check if arg 1 is valid number
			if(isPositiveInteger(args[0])) {
				converter.printTopList(sender, args[0], "");
				return true;
			}
			//check if arg 1 is valid timeFormat string
			if(converter.isTimeFormat(args[0])) {
				converter.printTopList(sender, "", args[0]);
				return true;
			}
			sender.sendMessage("Invalid parameter!");
			return false;
		}
		converter.printTopList(sender, "", "");
		return true;
	}

	private boolean isPositiveInteger(String number) {
		if(!NumberUtils.isDigits(number)) {
			return false;
		}
		int integer = Integer.parseInt(number);
		return integer>=1;
	}
}
