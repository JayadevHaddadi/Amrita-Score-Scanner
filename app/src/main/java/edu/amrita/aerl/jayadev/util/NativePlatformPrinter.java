package edu.amrita.aerl.jayadev.util;

import android.util.Log;

public class NativePlatformPrinter {
    public static boolean debugMode = true;
    public static boolean savingForPrint = false;
    public static String toPrintOut = "";
	
	public static void p(String s, String TAG, int pLevel) {
		if(debugMode) {
            Log.i(TAG, s);
            if (savingForPrint)
                toPrintOut += s + "\n";
        }
	}
}
