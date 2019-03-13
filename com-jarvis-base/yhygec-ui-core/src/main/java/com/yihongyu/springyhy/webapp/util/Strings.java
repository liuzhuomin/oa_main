package com.yihongyu.springyhy.webapp.util;

public final class Strings {
	public static final String EMPTY = "";

	public static String dropHead(final String source, final String drop, final String... moreDrops) {
		if (source == null || drop == null) {
			return source;
		}
		String result = dropHeadIt(source, drop);
		if (moreDrops == null || moreDrops.length == 0) {
			return result;
		}
		for (String more : moreDrops) {
			result = dropHeadIt(result, more);
		}
		return result;
	}

	public static String takeUntil(String source, char spChar) {
		return takeUntil(source, spChar, 1);
	}

	public static String takeUntil(String source, char spChar, final int count) {
		if (source == null || source.isEmpty()) {
			return source;
		}
		StringBuilder sb = new StringBuilder();
		int cnt = 0;
		for (int i = 0, len = source.length(); i < len; i++) {
			final char c = source.charAt(i);
			if (c == spChar) {
				cnt++;
				if (cnt == count) {
					break;
				}
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static String dropHeadIt(final String source, final String drop) {
		if (source == null || drop == null) {
			return source;
		}
		if (!source.startsWith(drop)) {
			return source;
		}

		return source.substring(drop.length());
	}

	private Strings() {
	}
}
