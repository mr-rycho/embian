package pl.rychu.embian.crawl.strategies.sort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2018-08-10 by rychu.
 */
public class SortNameCalculator {

	private static final Map<Character, Character> PL_MAP = new HashMap<>();

	static {
		PL_MAP.put('ą', 'a');
		PL_MAP.put('ć', 'c');
		PL_MAP.put('ę', 'e');
		PL_MAP.put('ł', 'l');
		PL_MAP.put('ń', 'n');
		PL_MAP.put('ó', 'o');
		PL_MAP.put('ś', 's');
		PL_MAP.put('ź', 'z');
		PL_MAP.put('ż', 'z');
	}

	private static final Pattern START_YM = Pattern.compile("^([0-9]{4}[-_][0-9]{2})( (.*))?$");

	private static final Pattern START_Y = Pattern.compile("^([0-9]{4})( (.*))?$");

	// ----------

	public Optional<String> calcSortName(String name) {
		String snA = noPlCase(name.toLowerCase());

		Matcher matcherYm = START_YM.matcher(snA);
		if (matcherYm.matches()) {
			int num = 100 * Integer.parseInt(matcherYm.group(1).replace("-", "").replace("_", ""));
			return Optional.of(String.format("%010d%s", num, nn(matcherYm.group(2))));
		}

		Matcher matcherY = START_Y.matcher(snA);
		if (matcherY.matches()) {
			int num = 10000 * Integer.parseInt(matcherY.group(1).replace("-", "").replace("_", ""));
			return Optional.of(String.format("%010d%s", num, nn(matcherY.group(2))));
		}

		return Optional.empty();
	}

	private static String noPlCase(String name) {
		if (!containsPl(name)) {
			return name;
		}

		int l = name.length();
		StringBuilder sb = new StringBuilder(l);
		for (int i = 0; i < l; i++) {
			Character c = name.charAt(i);
			sb.append(PL_MAP.getOrDefault(c, c));
		}
		return sb.toString();
	}

	private static boolean containsPl(String name) {
		int l = name.length();
		for (int i = 0; i < l; i++) {
			if (PL_MAP.containsKey(name.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	private static String nn(String s) {
		return s != null ? s : "";
	}

}
