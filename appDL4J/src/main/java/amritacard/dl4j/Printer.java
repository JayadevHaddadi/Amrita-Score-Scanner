package amritacard.dl4j;

import java.util.HashMap;

public class Printer {
	private static final String defaultTAG = "AmritaCard";
	private static final String cameraTAG = "AC-Camera";
	private static final String errorTAG = "AC-Error";
	private static final String debugTag = "AC-Debug";

	public static HashMap<String, Long> times = new HashMap<String, Long>();
	public static String po = "";
	public static final int pLevel1 = 1, pLevel2 = 2, pLevel3 = 3;
	public static int priorityLevel = pLevel1;

	public static void po(String s) {
		po += s;
	}

	public static void p(String s) {
		p(po + s, defaultTAG);
		po = "";
	}
	public static void p() {
		p(po, defaultTAG);
		po = "";
	}
	public static void po(int s) {
		po += s;
	}
	public static void p(int s) {
		p(po + s, defaultTAG);
		po = "";
	}
	public static void po(float s) {
		po += s;
	}
	public static void p(float s) {
		p(po + s, defaultTAG);
		po = "";
	}
	public static void po(double s) {
		po += s;
	}
	public static void p(double s) {
		p(po + s, defaultTAG);
		po = "";
	}

	public static void p(String text, int nrOfBreakLinesBefore) {
		String breaklines = "";
		for (int j = 0; j < nrOfBreakLinesBefore; j++) {
			breaklines += "\n";
		}
		p(breaklines + text);
	}


	public static void e(String text) {
		p(text, "test");
	}

	public static void w(String s) {
		p(s, "websocket");
	}

	public static int tGet(String timeName) {
		return tGet(timeName, false);
	}

	public static int tGet(String timeName, boolean keepAlive) {
		int diff = 0;
		long now = System.currentTimeMillis();
		diff = (int) (now - times.get(timeName));
		if (keepAlive)
			times.put(timeName, now);
		return diff;
	}

	public static void tStart(String timeName) {
		times.put(timeName, System.currentTimeMillis());
		p(timeName + " started...", 1);
	}

	public static void tEnd(String timeName) {
		p(timeName + " time: " + tGet(timeName, false) + " milliseconds");
	}

	public static void cam(String s) {
		p(s, defaultTAG);
		p(s, cameraTAG);
	}

	public static void debug(String s) {
		p(s, defaultTAG);
		p(s, debugTag);
	}

	public static void error(String s) {
		p(s, defaultTAG);
		p(s, errorTAG);
	}

	public static void checkBoolean(boolean rightAnswer, boolean checkAnswer,
			String string) {
		if (rightAnswer == checkAnswer)
			Printer.p(string + "\tOK");
		else
			Printer.p(string);
	}

	public static void highP(String string) {
		NativePlatformPrinter.p(string, defaultTAG, pLevel2);
	}

	public static void p(String s, String TAG) {
		NativePlatformPrinter.p(s, TAG, pLevel1);
	}
}
