package to.us.harha.twitchai.bot;

import java.util.ArrayList;

public class TwitchChannel
{

    private String                m_name;
    private ArrayList<TwitchUser> m_users;
    private int                   m_cmd_sent;

    public TwitchChannel(String name)
    {
        m_name = name;
        m_users = new ArrayList<TwitchUser>();
        m_cmd_sent = 0;
    }

    @Override
    public String toString()
    {
        return "TwitchChannel[" + m_name + ", " + m_users.size() + ", " + m_cmd_sent + "]";
    }

    public String getName()
    {
        return m_name;
    }

    public void setName(String name)
    {
        m_name = name;
    }

    public void addUser(TwitchUser user)
    {
        m_users.add(user);
    }

    public void delUser(TwitchUser user)
    {
        m_users.remove(user);
    }

    public ArrayList<TwitchUser> getUsers()
    {
        return m_users;
    }

    public ArrayList<TwitchUser> getOperators()
    {
        ArrayList<TwitchUser> result = new ArrayList<TwitchUser>();

        for (TwitchUser u : m_users)
        {
            if (u.isOperator())
            {
                result.add(u);
            }
        }

        return result;
    }

    public ArrayList<TwitchUser> getModerators()
    {
        ArrayList<TwitchUser> result = new ArrayList<TwitchUser>();

        for (TwitchUser u : m_users)
        {
            if (u.isModerator())
            {
                result.add(u);
            }
        }

        return result;
    }

    public TwitchUser getUser(String name)
    {
        TwitchUser result = null;

        for (TwitchUser u : m_users)
        {
            if (u.getName().equals(name))
            {
                result = u;
                break;
            }
        }

        return result;
    }

    public TwitchUser getOperator(String name)
    {
        TwitchUser result = null;

        for (TwitchUser u : getOperators())
        {
            if (u.getName().equals(name))
            {
                result = u;
                break;
            }
        }

        return result;
    }

    public TwitchUser getModerator(String name)
    {
        TwitchUser result = null;

        for (TwitchUser u : getModerators())
        {
            if (u.getName().equals(name))
            {
                result = u;
                break;
            }
        }

        return result;
    }

    public int getCmdSent()
    {
        return m_cmd_sent;
    }

    public void setCmdSent(int cmd_sent)
    {
        m_cmd_sent = cmd_sent;
    }

}
