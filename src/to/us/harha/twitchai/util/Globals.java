package to.us.harha.twitchai.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class Globals
{

    // Config
    public static int            g_window_width;
    public static int            g_window_height;
    public static boolean        g_debug;
    public static String         g_bot_name;
    public static String         g_bot_oauth;
    public static String         g_bot_chan;
    public static String         g_bot_version   = "TwitchAI 0.0.2";
    public static String         g_lib_version   = "PircBot 1.5.0";

    // Time & Date
    public static DateFormat     g_dateformat    = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static Date           g_date          = new Date();

    // Global variables
    public static final String   g_commands_user = "!help !info !time !users !ops !mods !channels !slots";
    public static final String   g_commands_mod  = "!addmod !delmod !joinchan !partchan !addchan";
    public static final String[] g_emotes_faces  = { "4Head", "BibleThump", "BloodTrail", "VaultBoy", "deIlluminati", "DOOMGuy", "FailFish", "Kappa", "Keepo" };

    // Server messages
    public static final String   g_server_memreq = "CAP REQ :twitch.tv/membership";
    public static final String   g_server_memans = ":tmi.twitch.tv CAP * ACK :twitch.tv/membership";

    // Java objects
    public static final Random   g_random        = new Random();

}
