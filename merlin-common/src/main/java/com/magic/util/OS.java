package com.magic.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OS {

	private static int selfPid = 0;

	public static int getpid() {
		if (selfPid != 0) {
			return selfPid;
		}
		synchronized (OS.class) {
			if (selfPid != 0) {
				return selfPid;
			}
			RuntimeMXBean rtb = ManagementFactory.getRuntimeMXBean();
			String processName = rtb.getName();
			Integer pid = tryPattern1(processName);

			if (pid == null) {
				throw new UnsupportedOperationException("cannot get pid");
			}

			selfPid = pid.intValue();
			return selfPid;
		}
	}

	/**
	 * from http://golesny.de/wiki/code:javahowtogetpid; that site has more
	 * suggestions if this fails...
	 */
	private static Integer tryPattern1(String processName) {
		Integer result = null;

		/* tested on: */
		/* - windows xp sp 2, java 1.5.0_13 */
		/* - mac os x 10.4.10, java 1.5.0 */
		/* - debian linux, java 1.5.0_13 */
		/* all return pid@host, e.g 2204@antonius */

		Pattern pattern = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(processName);
		if (matcher.matches()) {
			result = Integer.valueOf(Integer.parseInt(matcher.group(1)));
		}
		return result;

	}

	public static boolean checkPidIsSelf(int pid) {
		return pid == selfPid;
	}
}
