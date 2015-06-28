package to.us.harha.twitchai.util;

import static to.us.harha.twitchai.util.Globals.*;
import static to.us.harha.twitchai.util.LogUtils.logMsg;
import static to.us.harha.twitchai.util.LogUtils.logErr;
import static to.us.harha.twitchai.util.GenUtils.exit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class ConfUtils
{

    public static void init()
    {
        File f = new File("data/config.cfg");
        if (f.exists() && !f.isDirectory())
        {
            load();
        }
        else
        {
            create();
            init();
        }
    }

    public static void create()
    {
        Properties p = new Properties();
        OutputStream o = null;
        try
        {
            o = new FileOutputStream("data/config.cfg");

            // Set each variable
            p.setProperty("g_debug", "false");
            p.setProperty("g_bot_reqMembership", "true");
            p.setProperty("g_bot_reqCommands", "true");
            p.setProperty("g_bot_reqTags", "false");
            p.setProperty("g_bot_name", "TwitchAI");
            p.setProperty("g_bot_oauth", "youroauth");
            p.setProperty("g_bot_chan", "#IllusionAI");

            // Store the variables
            p.store(o, null);

            // Close the outputstream object
            o.close();

            logMsg("config.cfg" + " Created succesfully!");
        } catch (IOException e)
        {
            logErr("Couldn't create the main configuration file, closing program...");
            exit(1);
        }
    }

    public static void load()
    {
        Properties p = new Properties();
        InputStream i = null;
        try
        {
            i = new FileInputStream("data/config.cfg");

            // Load the file
            p.load(i);

            // Get the properties and set the config variables
            g_debug = Boolean.valueOf(p.getProperty("g_debug"));
            g_bot_reqMembership = Boolean.valueOf(p.getProperty("g_bot_reqMembership"));
            g_bot_reqCommands = Boolean.valueOf(p.getProperty("g_bot_reqCommands"));
            g_bot_reqTags = Boolean.valueOf(p.getProperty("g_bot_reqTags"));
            g_bot_name = String.valueOf(p.getProperty("g_bot_name"));
            g_bot_oauth = String.valueOf(p.getProperty("g_bot_oauth"));
            g_bot_chan = String.valueOf(p.getProperty("g_bot_chan"));

            // Close the inputstream object
            i.close();

            logMsg("config.cfg" + " loaded succesfully!");
        } catch (IOException e)
        {
            logErr("Couldn't load the main configuration file, closing program...");
            exit(1);
        }
    }

}