package utils;

public class Logger {
    public static void info(Object info) {
        System.out.println(getThreadID() + ": " + info);
    }
    public static void error(Object msg)
    {
        System.err.println(getThreadID() + ": " + msg);
    }

    private static String getThreadID()
    {
        return Thread.currentThread().getName();
    }
}
