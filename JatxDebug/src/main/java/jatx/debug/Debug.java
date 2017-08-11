package jatx.debug;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Debug {
	public static boolean isCustomHandler = false;
	
    private static Thread.UncaughtExceptionHandler defaultUEH;
    private static Thread.UncaughtExceptionHandler customUEH;

	private static File sLogDir;

	public static void setSimpleExceptionHandler() {
		if (isCustomHandler) {
			Log.e("debug", "custom handler already set");
			return;
		}
		Log.e("debug", "setting custom handler");

		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		customUEH = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(final Thread thread, final Throwable ex) {
				Log.e("uncaught error", Debug.exceptionToString(ex));
				defaultUEH.uncaughtException(thread, ex);
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(customUEH);

		isCustomHandler = true;
	}

	public static void setCustomExceptionHandler(File logDir) {
		if (isCustomHandler) {
			Log.e("debug", "custom handler already set");
			return;
		}
		Log.e("debug", "setting custom handler");

		sLogDir = logDir;

		defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
		customUEH = new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(final Thread thread, final Throwable ex) {
				Log.e("uncaught error", Debug.exceptionToString(ex));
				Debug.exceptionToFile(ex, "fatal_error_", sLogDir);
				//Debug.exceptionToDeveloper(ex, "Fatal Error");

				defaultUEH.uncaughtException(thread, ex);
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(customUEH);

		isCustomHandler = true;
	}

	public static String exceptionToString(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}
	
	public static void exceptionToFile(Throwable e, String prefix, File logDir) {
	    SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HH_mm_ss");
	     
	    File errorDump = new File(
	    		 logDir.getAbsolutePath()
	    		 + File.separator
	    		 + prefix
	    		 + sdf.format(new Date())
	    		 + ".txt");
	     
	    try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(errorDump));
			pw.println(exceptionToString(e));
			pw.close();
	    } catch (FileNotFoundException ex) {}
	}
	
	/*
	public static void exceptionToDeveloper(Throwable e, String title) {
		MailHelper.sendEmailToDev(title, exceptionToString(e));
	} 
	*/
	
	public static void logToFile(String data, File logDir) {
	    logDir.mkdirs();
	     
	    SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_HH_mm_ss");
	     
	    File errorDump = new File(
	    		 logDir.getAbsolutePath()
	    		 + File.separator
	    		 + "data_log_"
	    		 + sdf.format(new Date())
	    		 + ".txt");
	     
	    try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(errorDump));
			pw.println(data);
			pw.close();
	    } catch (FileNotFoundException ex) {}
	}
}
