package me.roopekoo.timeTracker.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Messages {
	TITLE("title", "&8[&3Time&cTracker&8] "),
	FILE_CREATE_FAIL1("file-create-fail1", "&cCouldn't create messages.yml file."),
	FILE_CREATE_FAIL2("file-create-fail2", "&cThis is a fatal error. Now disabling!"),
	SAVE_FAIL1("save-fail1", "&cFailed to save messages.yml"),
	SAVE_FAIL2("save-fail2", "&cReport this stack trace to Roopekoo."),
	NO_PERM("no-permission", "&cYou do not have permission to do that!"),
	TOO_MANY_PARAMS("too-many-parameters", "&cToo many parameters!"),
	NOT_ENOUGH_PARAMS("not-enough-parameters", "&cNot enough parameters!"),
	INVALID_USERNAME("invalid-username", "&cInvalid username"),
	INVALID_PAGE_NO("invalid-page-number", "&cInvalid page number!"),
	INVALID_PAGE("invalid page", "&cThat page does not exist!"),
	INVALID_PARAM("invalid-parameter", "&cInvalid parameter!"),
	INVALID_TIME_FORMAT("invalid-time-format", "&cInvalid time format!"),
	HISTORY_SELF("history-self", "&2Your &aplaytime in this &e{0} &ais &6{1}"),
	HISTORY("history", "{0}{1} &ahas a playtime of &6{2} &ain this {3}"),
	FORCE_UPDATED("force-updated", "&aPlaytime toplist has been &cforce&6-updated&a!"),
	LIST_UPDATE("list-update", "&aUpdating top list... &ePlease wait&a!"),
	TOPLIST_TITLE("toplist-title", "&aPlaytime toplist &e-- &6Page &c{0}&6/&c{1}"),
	TOPLIST_MAIN("toplist-main", "&f{0}. {1}{2}&e: &6{3}"),
	TOPLIST_FOOTER("toplist-footer", "&aProceed to the next page with &e/timeTracker {0}"),
	GETTIME("gettime", "{0}{1} &ahas a playtime of &6{2}"),
	GETTIME_SELF("gettime-self", "&2Your &aplaytime is &6{0}"),
	GETTIME_TOTAL("gettime-total", "&aCombined total playtime is &6{0}"),
	TPH_TITLE("tph-title", "&aPlaytime toplist history in this &6{0} &e-- &6Page &c{1}&6/&c{2}"),
	PLAYER_REQUIRED("player-required", "&cPlayername required!"),
	ONLINE("online", "&2"),
	OFFLINE("offline", "&8"),
	DAY("day", "&eday"),
	MONTH("month", "&emonth"),
	YEAR("year", "&eyear");

	private static YamlConfiguration MSG;
	private final String def;
	private final String path;

	Messages(String path, String message) {
		this.path = path;
		this.def = message;
	}

	public static void setFile(YamlConfiguration messages) {
		MSG = messages;
	}

	public static Messages fromString(String code) {
		for(Messages e: Messages.values()) {
			if(e.path.equals(code))
				return e;
		}
		return null;
	}

	@Override public String toString() {
		String s = MSG.getString(this.path, def);
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public String getPath() {
		return this.path;
	}

	public Object getDefault() {
		return this.def;
	}
}
