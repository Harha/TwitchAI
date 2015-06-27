package to.us.harha.twitchai.bot;

public class ChanUser
{

    private final String m_name;
    private String       m_prefix;
    private String       m_channel;
    private int          m_cmd_timer;
    private boolean      m_urlpermit;

    public ChanUser(String name, String channel)
    {
        m_name = name;
        m_prefix = "";
        m_channel = channel;
        m_cmd_timer = 0;
        m_urlpermit = false;
    }

    @Override
    public String toString()
    {
        return "ChanUser[" + m_prefix + m_name + ", " + m_cmd_timer + "]";
    }

    public String getName()
    {
        return m_name;
    }

    public String getPrefix()
    {
        return m_prefix;
    }

    public String getChannel()
    {
        return m_channel;
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

    public void setChannel(String channel)
    {
        m_channel = channel;
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
