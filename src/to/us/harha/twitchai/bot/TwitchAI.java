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

    private boolean               m_hasMembership;
    private ArrayList<TwitchUser> m_moderators;
    private ArrayList<ChanUser>   m_chanusers;

    public TwitchAI()
    {
        m_hasMembership = false;
        m_moderators = new ArrayList<TwitchUser>();
        m_chanusers = new ArrayList<ChanUser>();

        setName(g_bot_name);
        setVersion(g_lib_version);
        setVerbose(false);
    }

    public void init_twitch()
    {
        logMsg("Loading all registered moderators...");
        ArrayList<String> loadedModerators = FileUtils.readTextFile("data/moderators.txt");
        for (String m : loadedModerators)
        {
            String[] m_split = m.split(" ");
            m_moderators.add(new TwitchUser(m_split[0], Integer.parseInt(m_split[1])));
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

        logMsg("Requesting twitch membership capability for JOIN/PART messages...");
        sendRawLine(g_server_memreq);
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
            init_channels();
            return;
        }

        String[] line_array = line.split(" ");

        if (line_array[0].equals("MODE"))
        {
            onMode(line_array[1], line_array[3], line_array[3], "", line_array[2]);
        }
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname)
    {
        super.onJoin(channel, sender, login, hostname);
    }

    @Override
    public void onPart(String channel, String sender, String login, String hostname)
    {
        ArrayList<ChanUser> users_to_be_removed = new ArrayList<ChanUser>();
        for (ChanUser u : m_chanusers)
        {
            if (u.getName().equals(sender) && u.getChannel().equals(channel))
            {
                logMsg("Removing " + sender + " from m_chanusers...");
                users_to_be_removed.add(u);
            }
        }
        m_chanusers.removeAll(users_to_be_removed);
    }

    @Override
    public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
    {
        super.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
    }

    @Override
    public void onUserList(String channel, User[] users)
    {
        super.onUserList(channel, users);

        for (User u : users)
        {
            if (getChanUserFromChan(channel, u.getNick()) == null)
            {
                ChanUser chanuser = new ChanUser(u.getNick(), channel);
                m_chanusers.add(chanuser);
                logMsg("Adding new chanuser..." + chanuser + " to m_chanusers");
            }
        }
    }

    @Override
    public void onMessage(String channel, String sender, String login, String hostname, String message)
    {
        logMsg("data/channels/" + channel, "/onMessage", "User: " + sender + " Hostname: " + hostname + " Message: " + message);

        /*
        if (message.contains("http://") || message.contains("www.") || message.contains(".com") || message.contains(".org"))
        {
            sendMessage(channel, "You need a permission to post links! " + sender + " Was put on a timeout.");
            sendMessage(channel, "/timeout " + login);
        }
        */

        // Handle all chat commands
        if (message.startsWith("!"))
        {
            ChanUser cmduser = getChanUserFromChan(channel, sender);

            if (cmduser != null && message.length() > 3)
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
                case "!help":
                    sendMessage(channel, "List of available commands to you: " + g_commands_user);

                    if (isMod(sender) != null)
                    {
                        sendMessage(channel, "List of extra commands available to you: " + g_commands_mod);
                    }
                    break;

                case "!info":
                    sendMessage(channel, "Core: " + g_bot_version + " Library: " + getVersion());
                    break;

                case "!time":
                    sendMessage(channel, g_dateformat.format(g_date));
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
                        FileUtils.writeToTextFile("data/", "slots.txt", g_dateformat.format(g_date) + " " + sender + ": " + slots);
                    }
                    break;

                case "!permit":
                    if (!getChanOps(channel).contains(getUserFromChan(channel, sender)))
                    {
                        break;
                    }

                    sendMessage(channel, "This works!");
                    break;

                case "!addmod":
                    if (isMod(sender) == null)
                    {
                        break;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !addmod username");
                        break;
                    }

                    user = msg_array[1].toLowerCase();

                    if (isMod(user) == null)
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
                    if (isMod(sender) == null)
                    {
                        break;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !delmod username");
                        break;
                    }

                    user = msg_array[1].toLowerCase();

                    if (isMod(user) != null)
                    {
                        logMsg(sender + " Removed a moderator: " + user + " Privileges: 1");
                        sendMessage(channel, sender + " Removed a moderator: " + user + " Privileges: 1");
                        // FileUtils.removeLineFromTextFile("moderators.txt", user + " " + m_moderators.get(user).getPrivileges());
                        m_moderators.remove(isMod(user));
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
                    if (isMod(sender) == null)
                    {
                        break;
                    }

                    if (msg_array.length <= 1)
                    {
                        sendMessage(channel, "Wrong syntax! Usage: !addchan channel");
                        break;
                    }

                    if (!msg_array[1].startsWith("#"))
                    {
                        msg_array[1] = "#" + msg_array[1];
                    }

                    ArrayList<String> channels = FileUtils.readTextFile("data/channels.txt");
                    if (channels.size() <= 0 || !channels.contains(msg_array[1]))
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
                    break;
            }

            if (cmduser == null)
            {
                cmduser = new ChanUser(sender, channel);
                m_chanusers.add(cmduser);
            }

            cmduser.setCmdTimer(5);
        }
    }

    @Override
    public void onPrivateMessage(String sender, String login, String hostname, String message)
    {
        logMsg("data", "/privmsg", "User: " + sender + " Hostname: " + hostname + " Message: " + message);

        String[] msg_array = message.split(" ");
        String msg_command = msg_array[0];
        String user;

        switch (msg_command)
        {
            case "!joinchan":
                cmdJoinChan("IllusionAI", sender, msg_array);
                break;

            case "!partchan":
                cmdPartChan("IllusionAI", sender, msg_array);
                break;
        }
    }

    @Override
    public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode)
    {
        super.onMode(channel, sourceNick, sourceLogin, sourceHostname, mode);

        if (getChanUserFromChan(channel, sourceNick) == null)
        {
            logErr("Error onMode! Cannot find requested user from m_chanusers! (" + sourceNick + ") in (" + channel + ")");
            // m_chanusers.add(new ChanUser(sourceNick, channel));
        }

        if (mode.equals("+o"))
        {
            logMsg("Adding +o MODE for user " + sourceNick + " in channel " + channel);
            getChanUserFromChan(channel, sourceNick).setPrefix("@");
        }
        else if (mode.equals("-o"))
        {
            logMsg("Adding -o MODE for user " + sourceNick + " in channel " + channel);
            getChanUserFromChan(channel, sourceNick).setPrefix("");
        }
    }

    @Override
    public void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        super.onOp(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    @Override
    public void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname, String recipient)
    {
        super.onDeop(channel, sourceNick, sourceLogin, sourceHostname, recipient);
    }

    public void cmdJoinChan(String channel, String sender, String[] msg_array)
    {
        if (isMod(sender) == null)
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
        if (isMod(sender) == null)
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

    public ArrayList<User> getAllUsers()
    {
        ArrayList<User> result = new ArrayList<User>();

        for (String s : getChannels())
        {
            result.addAll(Arrays.asList(getUsers(s)));
        }

        return result;
    }

    public ArrayList<User> getChanUsers(String channel)
    {
        ArrayList<User> result = new ArrayList<User>();
        result.addAll(Arrays.asList(getUsers(channel)));
        return result;
    }

    public ArrayList<ChanUser> getAllOps()
    {
        ArrayList<ChanUser> ops = new ArrayList<ChanUser>();

        for (ChanUser u : getAllChanUsers())
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

        for (ChanUser u : getChanChanUsers(channel))
        {
            if (u.getPrefix().equals("@"))
            {
                ops.add(u);
            }
        }

        return ops;
    }

    public ArrayList<ChanUser> getAllChanUsers()
    {
        ArrayList<ChanUser> result = new ArrayList<ChanUser>();

        for (String s : getChannels())
        {
            result.addAll(getChanChanUsers(s));
        }

        return result;
    }

    public ArrayList<ChanUser> getChanChanUsers(String channel)
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

    public User getUserFromAll(String nick)
    {
        User result = null;

        for (User u : getAllUsers())
        {
            if (u.getNick().equals(nick))
            {
                result = u;
            }
        }

        return result;
    }

    public User getUserFromChan(String channel, String nick)
    {
        User result = null;

        for (User u : getChanUsers(channel))
        {
            if (u.getNick().equals(nick))
            {
                result = u;
            }
        }

        return result;
    }

    public ChanUser getChanUserFromChan(String channel, String nick)
    {
        ChanUser result = null;

        for (ChanUser u : getChanChanUsers(channel))
        {
            if (u.getName().equals(nick))
            {
                result = u;
            }
        }

        return result;
    }

    public TwitchUser isMod(String nick)
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

    public ArrayList<TwitchUser> getMods()
    {
        return m_moderators;
    }

}
