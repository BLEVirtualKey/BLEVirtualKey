package com.virtualkey.logview;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.widget.ListView;

public class LogView
{
	private final int DEFAULT_MAX_ITEMS = 500;
	
	private Context m_context = null;
	private ListView m_listview = null;
	
	private LogItemsAdapter m_adapter = null;
	private int m_nMaxItems = DEFAULT_MAX_ITEMS;
	private LogItemLevel m_levelPrevious = LogItemLevel.V;
	
	private boolean m_fTimeStamp = false;
	
	
	public int getMaxItems ()
	{
		return m_nMaxItems;
	}

	public void setMaxItems (int p_nMaxItems)
	{
		this.m_nMaxItems = p_nMaxItems;
	}

	
	public boolean isTimeStamp ()
	{
		return m_fTimeStamp;
	}

	public void setTimeStamp (boolean p_fTimeStamp)
	{
		this.m_fTimeStamp = p_fTimeStamp;
	}

	public LogView (Context p_context, ListView p_listview)
	{
		m_context = p_context;
		m_listview = p_listview; 
		
		init ();
	}

	public void init ()
	{
		m_listview.setBackgroundColor (Color.LTGRAY);
		m_listview.setCacheColorHint (Color.LTGRAY);
		
		// Remove the separator between items
		m_listview.setDivider (null);
		m_listview.setDividerHeight (0);

		m_adapter = new LogItemsAdapter (m_context, new ArrayList<LogItem> (m_nMaxItems));
		m_listview.setAdapter (m_adapter);
		m_adapter.clear ();
	}

	public void clear ()
	{
		m_adapter.clear ();
	}
	
	public void log (final String p_strMsg, LogItemLevel p_loglevel)
	{
		if (m_adapter.getCount () > m_nMaxItems)
		{
			m_adapter.remove (0);
		}

		if (p_loglevel == null)
			p_loglevel = m_levelPrevious;
		else
			m_levelPrevious = p_loglevel;

		LogItem item;
		if (isTimeStamp () == true)
		{
        	String strTimeFormat = "HH:mm:ss.SSS ";
        	SimpleDateFormat 	df = new SimpleDateFormat (strTimeFormat);
        	item = new LogItem (df.format (new Date (System.currentTimeMillis())) + p_strMsg, p_loglevel);
		}
		else
			item = new LogItem (p_strMsg, p_loglevel);
		
		m_adapter.add (item);
		
		// Just make sure the last line is visible
		m_listview.setSelection (m_adapter.getCount () - 1);
	}

	public void log (final String p_strMsg)
	{
		if (p_strMsg.length() <= 2)
			log (p_strMsg, null);
				
		// Try to find a the log level char
		if (p_strMsg.charAt (1) == '/')
		{
			LogItemLevel logLevel = LogItemLevel.getLevelFromCharID (p_strMsg.charAt (0));
			if (logLevel != null)
				log (p_strMsg.substring(2), logLevel);
			else
				log (p_strMsg, null);
		}
		else
			log (p_strMsg, null);
	}
}
