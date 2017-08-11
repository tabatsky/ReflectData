package jatx.debug;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by jatx on 28.07.17.
 */

public class Log {

    public static void e(String tag, String msg) {
        logMsg("error", tag, msg);
    }

    public static void w(String tag, String msg) {
        logMsg("warning", tag, msg);
    }

    public static void i(String tag, String msg) {
        logMsg("info", tag, msg);
    }

    private static void logMsg(String level, String tag, String msg) {
        try {
            Class logClass = Class.forName("android.util.Log");
            String methodName = level.substring(0, 1).toLowerCase();
            Method logMethod = logClass.getDeclaredMethod(methodName, new Class[]{String.class, String.class});
            logMethod.invoke(null, tag, msg);
        } catch (ClassNotFoundException e) {
            System.err.println("[" + level + "|" + tag + "]" + msg);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
