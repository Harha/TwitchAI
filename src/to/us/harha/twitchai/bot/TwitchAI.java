package to.us.harha.twitchai.bot;

import static to.us.harha.twitchai.util.Globals.*;
import static to.us.harha.twitchai.util.LogUtils.logMsg;
import static to.us.harha.twitchai.util.LogUtils.logErr;
import static to.us.harha.twitchai.util.GenUtils.exit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import to.us.harha.twitchai.util.FileUtils;

public class TwitchAI extends PircBot
{

    private float                 m_cycleTime;
    private boolean               m_hasMembership;
    private boolean               m_hasCommands;
    private boolean               m_hasTags;
    private ArrayList<TwitchUser> m_moderators;
    private ArrayList<ChanUser>   m_chanusers;

    public TwitchAI()
    {
        m_cycleTime = 0.0f;
        m_hasMembership = false;
        m_hasCommands = false;
        m_hasTags = false;
        m_moderators = new ArrayList<TwitchUser>();
        m_chanusers = new ArrayList<ChanUser>();

        setName(g_bot_name);
        setVersion(g_lib_version);
        setVerbose(false);
    }

    public void destruct()
    {
        logMsg("TwitchAI Shutting down! Doing a cleanup...");
        logMsg("Clearing all m_chanusers...");
        m_chanusers.clear();
        logMsg("Clearing all m_moderators...");
        m_moderators.clear();
    }

    public void init_twitch()
    {
        logMsg("Loading all registered moderators...");
        ArrayList<String> loadedModerators = FileUtils.readTextFile("data/moderators.txt");
        for (String m : loadedModerators)
        {
            String[] m_split = m.split(" ");
            TwitchUser newmod = new TwitchUser(m_split[0], Integer.parseInt(m_split[1]));
            logMsg("Loading a TwitchAI moderator: " + newmod);
            m_moderators.add(newmod);
        }

        logMsg("Attempting to connect to irc.twitch.tv...");
        try
        {
            connect("irc.twitch.tv", 6667, g_bot_oauth);
        } catch (IOException | IrcException e)
        {
            logErr(e.getStackTrace().toString());
            exit(1);
        }

        logMsg("Requesting twitch membership capability for NAMES/JOIN/PART/MODE messages...");
        sendRawLine(g_server_memreq);
        logMsg("Requesting twitch commands capability for NOTICE/HOSTTARGET/CLEARCHAT/USERSTATE messages... ");
        sendRawLine(g_server_cmdreq);
        logMsg("Requesting twitch tags capability for PRIVMSG/USERSTATE/GLOBALUSERSTATE messages... ");
        sendRawLine(g_server_tagreq);
    }

    public void init_channels()
    {
        logMsg("Attempting to join all registered channels...");
        ArrayList<String> loadedChannels = FileUtils.readTextFile("data/channels.txt");
        for (String c : loadedChannels)
        {
            if (!c.startsWith("#"))
            {
                c = "#" + c;
            }

            logMsg("Initial join to a registered channel: " + c);
            joinChannel(c);
        }
    }

    @Override
    public void handleLine(String line)
    {
        if (line.contains(":jtv "))
        {
            line = line.replace(":jtv ", "");
        }

        logMsg("handleLine | " + line);

        super.handleLine(line);

        if (!m_hasMembership && line.equals(g_server_memans))
        {
            m_hasMembership = true;
        }

        if (!m_hasCommands && line.equals(g_server_cmdans))
        {
            m_hasCommands = true;
        }

        if (!m_hasTags && line.equals(g_server_tagans))
        {
            m_hasTags = true;
        }

        String[] line_array = line.split(" ");

        if (line_array[0].equals("MODE") && line_array.length >= 4)
        {
            onMode(line_array[1], line_array[3], line_array[3], "", line_array[2]);
        }
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname)
    {
        super.onJoin(channel, sender, login, hostname);

        if (getUserFromChan(channel, sender) == null)
        {
            ChanUser chanuser = new ChanUser(sender, channel);
            m_chanusers.add(chanuser);
            logMsg("Adding new chanuser..." + chanuser + " to m_chanusers for channel " + channel);
        }
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname)
    {
        super.onPart(channel, sender, login, hostname);

        ArrayList<ChanUser> users_to_be_removed = new ArrayList<ChanUser>();
        for (ChanUser u : m_chanusers)
        {
            if (u.getName().equals(sender) && u.getChannel().equals(channel))
            {
                logMsg("Removing " + u + " from m_chanusers...");
                users_to_be_removed.add(u);
            }
        }
        m_chanusers.removeAll(users_to_be_removed);
    }

    @Override
    public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode)
    {
        super.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);

        if (getUserFromChan(channel, sourceNick) == null)
        {
            logErr("Error onMode! Cannot find requested user from m_chanusers! (" + sourceNick + ") in (" + channel + ")");
            return;
        }

        if (mode.equals("+o"))
        {
            logMsg("Adding +o MODE for user " + sourceNick + " in channel " + channel);
            getUserFromChan(channel, sourceNick).setPrefix("@");
        }
        else if (mode.equals("-o"))
        {
            logMsg("Adding -o MODE for user " + sourceNick + " in channel " + channel);
            getUserFromChan(channel, sourceNick).setPrefix("");
        }
    }

    @Override
    public void onUserList(String channel, User[] users)
    {
        super.onUserList(channel, users);

        for (User u : users)
        {
            if (getUserFromChan(channel, u.getNick()) == null)
            {
                ChanUser chanuser = new ChanUser(u.getNick(), channel);
                m_chanusers.add(chanuser);
                logMsg("Adding new chanuser..." + chanuser + " to m_chanusers for channel " + channel);
            }
        }
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message)
    {
        logMsg("data/channels/" + channel, "/onMessage", "User: " + sender + " Hostname: " + hostname + " Message: " + message);

        /*
         * Handle all chat commands
         */
        if (message.startsWith("!"))
        {
            ChanUser cmduser = getUserFromChan(channel, sender);

            if (cmduser == null)
            {
                logErr("Error! Cannot find cmduser " + sender + " from channel " + channel + "! Assigning a null user object to cmduser.");
                cmduser = g_nulluser;
            }

            if (message.length() > 3)
            {
                if (cmduser.getCmdTimer() > 0)
                {
                    sendMessage(channel, "Please wait " + cmduser.getCmdTimer() + " seconds before sending a new command.");
                    return;
                }
            }

            String[] msg_array = message.split(" ");
            String msg_command = msg_array[0];
            String user;

            switch (msg_command)
            {

            /*
             * Normal channel user commands below
             */
                case "!help":
                    sendMessage(channel, "List of available commands to you: " + g_commands_user);

                    if (cmduser.getPrefix().equals("@"))
                    {
                        sendMessage(channel, "List of available operator commands to you: " + g_commands_op);
                    }

                    if (getMod(sender) != null)
                    {
                        sendMessage(channel, "List of TwitchAI moderator commands available to you: " + g_commands_mod);
                    }
                    break;

                case "!info":
                    sendMessage(channel, "Language: Java Core: " + g_bot_version + " Library: " + getVersion());
                    break;

                case "!performance":
                    sendMessage(channel, "My current main-loop cycle time: " + m_cycleTime + "ms.");
                    break;

                case "!date":
                    sendMessage(channel, g_dateformat.format(g_date));
                    break;

                case "!time":
                    sendMessage(channel, g_timeformat.format(g_date));
                    break;

                case "!users":
                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Users in this channel: " + getChanUsers(channel).size());
                        break;
                    }

                    if (msg_array[1].equals("all"))
                    {
                        sendMessage(channel, "Users in all channels: " + getAllUsers().size());
                        break;
                    }

                    if (!msg_array[1].startsWith("#"))
                    {
                        msg_array[1] = "#" + msg_array[1];
                    }

                    sendMessage(channel, "Users in channel " + msg_array[1] + ": " + getChanUsers(msg_array[1]).size());
                    break;

                case "!ops":
                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Operators in this channel: " + getChanOps(channel));
                        break;
                    }

                    if (msg_array[1].equals("all"))
                    {
                        sendMessage(channel, "Operators in all channels: " + getAllOps().size());
                        break;
                    }

                    if (!msg_array[1].startsWith("#"))
                    {
                        msg_array[1] = "#" + msg_array[1];
                    }

                    sendMessage(channel, "Operators in channel " + msg_array[1] + ": " + getChanOps(msg_array[1]));
                    break;

                case "!mods":
                    sendMessage(channel, "TwitchAI Moderators: " + m_moderators);
                    break;

                case "!channels":
                    sendMessage(channel, "Registered channels: " + getChannels().length);
                    break;

                case "!slots":
                    int num1 = (int) (Math.random() * g_emotes_faces.length);
                    int num2 = (int) (Math.random() * g_emotes_faces.length);
                    int num3 = (int) (Math.random() * g_emotes_faces.length);
                    String slots = g_emotes_faces[num1] + " | " + g_emotes_faces[num2] + " | " + g_emotes_faces[num3];
                    sendMessage(channel, slots);
                    if (num1 == num2 && num2 == num3)
                    {
                        sendMessage(channel, "And we have a new winner! " + sender + " Just got their name on the slots legends list!");
                        FileUtils.writeToTextFile("data/", "slots.txt", g_datetimeformat.format(g_date) + " " + sender + ": " + slots);
                    }
                    break;

                /*
                 * Normal channel operator commands below
                 */
                case "!permit":
                    if (!getUserFromChan(channel, sender).getPrefix().equals("@"))
                    {
                        return;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !permit username");
                        return;
                    }

                    user = msg_array[1];
                    ChanUser permituser = getUserFromChan(channel, user);
                    if (permituser == null)
                    {
                        logErr("Error! User not found, cannot give permission for posting url's to selected user!");
                        return;
                    }

                    sendMessage(channel, sender + " Gave " + user + " a permission to post links!");
                    permituser.setUrlPermit(true);
                    break;

                case "!unpermit":
                    if (!getUserFromChan(channel, sender).getPrefix().equals("@"))
                    {
                        return;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !unpermit username");
                        return;
                    }

                    user = msg_array[1];
                    ChanUser unpermituser = getUserFromChan(channel, user);
                    if (unpermituser == null)
                    {
                        logErr("Error! User not found, cannot remove permission for posting url's from selected user!");
                        return;
                    }

                    sendMessage(channel, sender + " Removed " + user + "'s permission to post links!");
                    unpermituser.setUrlPermit(false);
                    break;

                /*
                 * Normal TwitchAI moderator commands below
                 */
                case "!addmod":
                    if (getMod(sender) == null)
                    {
                        break;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !addmod username");
                        break;
                    }

                    user = msg_array[1].toLowerCase();

                    if (getMod(user) == null)
                    {
                        logMsg(sender + " Added a new moderator: " + user + " Privileges: 1");
                        sendMessage(channel, sender + " Added a new moderator: " + user + " Privileges: 1");
                        FileUtils.writeToTextFile("data/", "moderators.txt", user + " 1");
                        m_moderators.add(new TwitchUser(user, 1));
                    }
                    else
                    {
                        logErr(sender + " Tried to add " + user + " as a moderator, but the user already is a moderator.");
                        sendMessage(channel, user + " Already is a moderator!");
                    }
                    break;

                case "!delmod":
                    if (getMod(sender) == null)
                    {
                        break;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !delmod username");
                        break;
                    }

                    user = msg_array[1].toLowerCase();

                    if (getMod(user) != null)
                    {
                        logMsg(sender + " Removed a moderator: " + user + " Privileges: 1");
                        sendMessage(channel, sender + " Removed a moderator: " + user + " Privileges: 1");
                        FileUtils.removeFromTextFile("data/", "moderators.txt", user + " " + getMod(user).getPrivileges());
                        m_moderators.remove(getMod(user));
                    }
                    else
                    {
                        logErr(sender + " Tried to remove a moderator: " + user + " that doesn't exist.");
                        sendMessage(channel, "a Moderator named " + user + " doesn't exist!");
                    }
                    break;

                case "!joinchan":
                    cmdJoinChan(channel, sender, msg_array);
                    break;

                case "!partchan":
                    cmdPartChan(channel, sender, msg_array);
                    break;

                case "!addchan":
                    cmdAddChan(channel, sender, msg_array);
                    break;
                case "!delchan":
                    cmdDelChan(channel, sender, msg_array);
                    break;
            }

            if (!cmduser.getName().equals("null"))
            {
                cmduser.setCmdTimer(5);
            }
        }
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message)
    {
        logMsg("data", "/privmsg", "User: " + sender + " Hostname: " + hostname + " Message: " + message);
    }

    public void cmdJoinChan(String channel, String sender, String[] msg_array)
    {
        if (getMod(sender) == null)
        {
            return;
        }

        if (msg_array.length <= 1)
        {
            sendMessage(channel, "Wrong syntax! Usage: !joinchan channel");
            return;
        }

        if (!msg_array[1].startsWith("#"))
        {
            msg_array[1] = "#" + msg_array[1];
        }

        logMsg(sender + " Requested a join to channel: " + msg_array[1]);
        joinChannel(msg_array[1]);
    }

    public void cmdPartChan(String channel, String sender, String[] msg_array)
    {
        if (getMod(sender) == null)
        {
            return;
        }

        if (msg_array.length <= 1)
        {
            sendMessage(channel, "Wrong syntax! Usage: !partchan channel");
            return;
        }

        if (!msg_array[1].startsWith("#"))
        {
            msg_array[1] = "#" + msg_array[1];
        }

        if (!Arrays.asList(getChannels()).contains(msg_array[1]))
        {
            logErr("Can't part channel " + msg_array[1] + " because it isn't in the joined channels list!");
            return;
        }

        logMsg(sender + " Requested a quit from channel: " + msg_array[1]);
        partChannel(msg_array[1]);
        ArrayList<ChanUser> users_to_be_removed = new ArrayList<ChanUser>();
        for (ChanUser u : m_chanusers)
        {
            if (u.getChannel().equals(msg_array[1]))
            {
                logMsg("Removing " + u.getName() + " from m_chanusers (" + msg_array[1] + ") because of a part request...");
                users_to_be_removed.add(u);
            }
        }
        m_chanusers.removeAll(users_to_be_removed);
        listChannels();
    }

    public void cmdAddChan(String channel, String sender, String[] msg_array)
    {
        if (getMod(sender) == null)
        {
            return;
        }

        if (msg_array.length <= 1)
        {
            sendMessage(channel, "Wrong syntax! Usage: !addchan channel");
            return;
        }

        if (!msg_array[1].startsWith("#"))
        {
            msg_array[1] = "#" + msg_array[1];
        }

        ArrayList<String> addchan_channels = FileUtils.readTextFile("data/channels.txt");
        if (addchan_channels.size() <= 0 || !addchan_channels.contains(msg_array[1]))
        {
            logMsg("Registering a new channel: " + msg_array[1]);
            FileUtils.writeToTextFile("data/", "channels.txt", msg_array[1]);
            joinChannel(msg_array[1]);
        }
        else
        {
            logErr("Failed to register a new channel: " + msg_array[1]);
            sendMessage(channel, "That channel is already registered!");
        }
        return;
    }

    public void cmdDelChan(String channel, String sender, String[] msg_array)
    {
        if (getMod(sender) == null)
        {
            return;
        }

        if (msg_array.length <= 1)
        {
            sendMessage(channel, "Wrong syntax! Usage: !delchan channel");
            return;
        }

        if (!msg_array[1].startsWith("#"))
        {
            msg_array[1] = "#" + msg_array[1];
        }

        if (!Arrays.asList(getChannels()).contains(msg_array[1]))
        {
            logErr("Can't delete channel " + msg_array[1] + " from the global channels list because it isn't in the joined channels list!");
            return;
        }

        logMsg(sender + " Requested a deletion of channel: " + msg_array[1]);
        partChannel(msg_array[1]);
        FileUtils.removeFromTextFile("data", "/channels.txt", msg_array[1]);
        ArrayList<ChanUser> users_to_be_removed = new ArrayList<ChanUser>();
        for (ChanUser u : m_chanusers)
        {
            if (u.getChannel().equals(msg_array[1]))
            {
                logMsg("Removing " + u.getName() + " from m_chanusers (" + msg_array[1] + ") because of a channel delete request...");
                users_to_be_removed.add(u);
            }
        }
        m_chanusers.removeAll(users_to_be_removed);
        listChannels();
    }

    public ArrayList<ChanUser> getAllUsers()
    {
        ArrayList<ChanUser> result = new ArrayList<ChanUser>();

        for (String s : getChannels())
        {
            result.addAll(getChanUsers(s));
        }

        return result;
    }

    public ArrayList<ChanUser> getChanUsers(String channel)
    {
        ArrayList<ChanUser> result = new ArrayList<ChanUser>();

        for (ChanUser u : m_chanusers)
        {
            if (u.getChannel().equals(channel))
            {
                result.add(u);
            }
        }

        return result;
    }

    public ArrayList<ChanUser> getAllOps()
    {
        ArrayList<ChanUser> ops = new ArrayList<ChanUser>();

        for (ChanUser u : getAllUsers())
        {
            if (u.getPrefix().equals("@"))
            {
                ops.add(u);
            }
        }

        return ops;
    }

    public ArrayList<ChanUser> getChanOps(String channel)
    {
        ArrayList<ChanUser> ops = new ArrayList<ChanUser>();

        for (ChanUser u : getChanUsers(channel))
        {
            if (u.getPrefix().equals("@"))
            {
                ops.add(u);
            }
        }

        return ops;
    }

    public ChanUser getUserFromAll(String nick)
    {
        ChanUser result = null;

        for (ChanUser u : getAllUsers())
        {
            if (u.getName().equals(nick))
            {
                result = u;
            }
        }

        return result;
    }

    public ChanUser getUserFromChan(String channel, String nick)
    {
        ChanUser result = null;

        for (ChanUser u : getChanUsers(channel))
        {
            if (u.getName().equals(nick))
            {
                result = u;
            }
        }

        return result;
    }

    public ArrayList<TwitchUser> getMods()
    {
        return m_moderators;
    }

    public TwitchUser getMod(String nick)
    {
        TwitchUser result = null;

        for (TwitchUser tu : m_moderators)
        {
            if (tu.getName().equals(nick))
            {
                result = tu;
            }
        }

        return result;
    }

    public float getCycleTime()
    {
        return m_cycleTime;
    }

    public void setCycleTime(float cycleTime)
    {
        m_cycleTime = cycleTime;
    }

    public boolean isInitialized()
    {
        return m_hasMembership & m_hasCommands & m_hasTags;
    }

}
