package to.us.harha.twitchai;

import static to.us.harha.twitchai.util.Globals.*;
import to.us.harha.twitchai.bot.ChanUser;
import to.us.harha.twitchai.bot.TwitchAI;
import to.us.harha.twitchai.util.ConfUtils;
import to.us.harha.twitchai.util.FileUtils;

public class Main
{

    public static void main(String[] args)
    {
        FileUtils.directoryExists("data");
        FileUtils.directoryExists("data/channels");
        ConfUtils.init();
        TwitchAI twitchai = new TwitchAI();
        twitchai.init_twitch();

        while (true)
        {
            g_date.setTime(System.currentTimeMillis());

            for (ChanUser u : twitchai.getAllChanUsers())
            {
                if (u.getCmdTimer() > 0)
                {
                    u.setCmdTimer(u.getCmdTimer() - 1);
                }
            }

            try
            {
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

}
