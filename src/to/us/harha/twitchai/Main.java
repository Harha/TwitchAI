package to.us.harha.twitchai;

import static to.us.harha.twitchai.util.Globals.*;
import static to.us.harha.twitchai.util.LogUtils.logMsg;
import static to.us.harha.twitchai.util.LogUtils.logErr;
import static to.us.harha.twitchai.util.GenUtils.exit;
import to.us.harha.twitchai.bot.TwitchChannel;
import to.us.harha.twitchai.bot.TwitchUser;
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

        int init_time = 5;
        while (!twitchai.isInitialized())
        {
            init_time--;
            try
            {
                logMsg("Waiting for twitch member/cmd/tag responses... " + init_time);
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if (!twitchai.isInitialized())
        {
            logErr("Failed to receive twitch member/cmd/tag permissions!");
            exit(1);
        }

        twitchai.init_channels();

        float time;
        long timeStart, timeEnd;

        while (twitchai.isConnected())
        {
            timeStart = System.nanoTime();
            g_date.setTime(System.currentTimeMillis());

            for (TwitchChannel c : twitchai.getTwitchChannels())
            {
                if (c.getCmdSent() > 0)
                {
                    c.setCmdSent(c.getCmdSent() - 1);
                }
            }

            for (TwitchUser u : twitchai.getAllUsers())
            {
                if (u.getCmdTimer() > 0)
                {
                    u.setCmdTimer(u.getCmdTimer() - 1);
                }
            }

            timeEnd = System.nanoTime();
            time = (float) (timeEnd - timeStart) / 1000000.0f;

            twitchai.setCycleTime(time);

            /*
             * Main loop ticks only once per second.
             */
            try
            {
                if (time < 1000.0f)
                {
                    Thread.sleep((long) (1000.0f - time));
                }
                else
                {
                    logErr("Warning! Main thread cycle time is longer than a second! Skipping sleep! Cycle-time: " + time);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
