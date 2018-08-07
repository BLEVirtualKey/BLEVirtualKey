package com.virtualkey.logview;


public class LogItem 
{
    private LogItemLevel m_level = LogItemLevel.V;
    private String m_strMsg = null;

    public LogItem (String p_strMsg, LogItemLevel p_level)
    {
    	this.m_strMsg = p_strMsg;
    	this.m_level = p_level;
    }
        
    public LogItemLevel getLevel ()
    {
    	return this.m_level;
    }

    public String getMsg ()
    {
    	return this.m_strMsg;
    }
}
