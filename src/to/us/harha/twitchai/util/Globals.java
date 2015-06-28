package to.us.harha.twitchai.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import to.us.harha.twitchai.bot.ChanUser;

public class Globals
{

    // Config
    public static int            g_window_width;
    public static int            g_window_height;
    public static boolean        g_debug;
    public static String         g_bot_name;
    public static String         g_bot_oauth;
    public static String         g_bot_chan;
    public static String         g_bot_version    = "TwitchAI 0.0.2";
    public static String         g_lib_version    = "PircBot 1.5.0";

    // Time & Date
    public static DateFormat     g_datetimeformat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static DateFormat     g_dateformat     = new SimpleDateFormat("dd.MM.yyyy");
    public static DateFormat     g_timeformat     = new SimpleDateFormat("HH:mm:ss");
    public static Date           g_date           = new Date();

    // Global variables
    public static final String   g_commands_user  = "!help !info !performance !date !time !users !ops !mods !channels !slots";
    public static final String   g_commands_op    = "!permit !unpermit";
    public static final String   g_commands_mod   = "!addmod !delmod !joinchan !partchan !addchan !delchan";
    public static final String[] g_emotes_faces   = { "4Head", "BibleThump", "BloodTrail", "VaultBoy", "deIlluminati", "DOOMGuy", "FailFish", "Kappa", "Keepo" };

    // Server messages
    public static final String   g_server_memreq  = "CAP REQ :twitch.tv/membership";
    public static final String   g_server_memans  = ":tmi.twitch.tv CAP * ACK :twitch.tv/membership";
    public static final String   g_server_cmdreq  = "CAP REQ :twitch.tv/commands";
    public static final String   g_server_cmdans  = ":tmi.twitch.tv CAP * ACK :twitch.tv/commands";
    public static final String   g_server_tagreq  = "CAP REQ :twitch.tv/tags";
    public static final String   g_server_tagans  = ":tmi.twitch.tv CAP * ACK :twitch.tv/tags";


    // Java objects
    public static final ChanUser g_nulluser       = new ChanUser("null", "null");

}
