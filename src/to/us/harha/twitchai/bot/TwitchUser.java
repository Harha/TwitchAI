package to.us.harha.twitchai.bot;

public class TwitchUser
{

    private String m_name;
    private int    m_privileges;

    public TwitchUser(String name, int privileges)
    {
        m_name = name;
        m_privileges = privileges;
    }

    @Override
    public String toString()
    {
        return "TwitchUser[" + m_name + ", " + m_privileges + "]";
    }

    public String getName()
    {
        return m_name;
    }

    public int getPrivileges()
    {
        return m_privileges;
    }

}
