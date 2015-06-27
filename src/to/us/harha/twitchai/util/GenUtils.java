package to.us.harha.twitchai.util;

import static to.us.harha.twitchai.util.LogUtils.logMsg;
import static to.us.harha.twitchai.util.LogUtils.logErr;

public class GenUtils
{

    public static void exit(int i)
    {
        if (i == 0)
        {
            logMsg("Exiting program with error code: " + i);
        }
        else
        {
            logErr("Exiting program with error code: " + i);
        }
        System.exit(i);
    }

}
