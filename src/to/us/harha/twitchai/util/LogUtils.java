package to.us.harha.twitchai.util;

import static to.us.harha.twitchai.util.Globals.*;
import static to.us.harha.twitchai.util.FileUtils.*;

public class LogUtils
{

    public static void logMsg(String msg)
    {
        System.out.print("[LOG]: " + msg + "\n");
        writeToTextFile("data", "/log.txt", g_dateformat.format(g_date) + ": " + msg);
    }

    public static void logMsg(String directory, String filename, String msg)
    {
        System.out.print("[LOG]: " + msg + "\n");
        writeToTextFile(directory, filename + "_LOG.txt", g_dateformat.format(g_date) + ": " + msg);
    }

    public static void logErr(String err)
    {
        System.err.print("[ERR]: " + err + "\n");
        writeToTextFile("data", "/err.txt", g_dateformat.format(g_date) + ": " + err);
    }

    public static void logErr(String directory, String filename, String err)
    {
        System.err.print("[ERR]: " + err + "\n");
        writeToTextFile(directory, filename + "_ERR.txt", g_dateformat.format(g_date) + ": " + err);
    }

}
