package me.roopekoo.timeTracker.utils;

import org.apache.commons.lang.math.NumberUtils;

public class Utilities {
	public boolean isPositiveInteger(String number) {
		if(!NumberUtils.isDigits(number)) {
			return false;
		}
		int integer = Integer.parseInt(number);
		return integer>=1;
	}
}
