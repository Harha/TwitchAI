package to.us.harha.twitchai.bot;

public class TwitchUser
{

    private final String m_name;
    private String       m_prefix;
    private int          m_cmd_timer;
    private boolean      m_urlpermit;

    public TwitchUser(String name, String prefix)
    {
        m_name = name;
        m_prefix = prefix;
        m_cmd_timer = 0;
        m_urlpermit = false;
    }

    @Override
    public String toString()
    {
        return "TwitchUser[" + m_prefix + m_name + ", " + m_cmd_timer + "]";
    }

    public void addPrefixChar(String prefix)
    {
        m_prefix = prefix + m_prefix;
    }

    public void delPrefixChar(String prefix)
    {
        m_prefix.replace(prefix, "");
    }

    public boolean isOperator()
    {
        return m_prefix.contains("@");
    }

    public boolean isModerator()
    {
        return m_prefix.contains("*") | isAdmin();
    }

    public boolean isAdmin()
    {
        return m_prefix.contains("&");
    }

    public String getName()
    {
        return m_name;
    }

    public String getPrefix()
    {
        return m_prefix;
    }

    public int getCmdTimer()
    {
        return m_cmd_timer;
    }

    public boolean getUrlPermit()
    {
        return m_urlpermit;
    }

    public void setPrefix(String prefix)
    {
        m_prefix = prefix;
    }

    public void setCmdTimer(int time)
    {
        m_cmd_timer = time;
    }

    public void setUrlPermit(boolean urlpermit)
    {
        m_urlpermit = urlpermit;
    }

}
