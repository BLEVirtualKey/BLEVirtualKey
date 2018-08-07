package com.virtualkey.logview;

import android.graphics.Color;


public enum LogItemLevel
{
	V (0, "#121212"),		// Verbose
	D (1, "#00006C"),		// Debug
	I (2, "#20831B"),		// Info
	W (3, "#FD7916"),		// Warning
	E (4, "#FD0010");		// Error
	
	private String m_strColorHex;
	private int m_nColorValue;
	private int m_nID;
	
	private LogItemLevel (int p_nID, String p_strColorHex)
	{
		m_nID = p_nID;
		m_strColorHex = p_strColorHex;
		m_nColorValue = Color.parseColor (p_strColorHex);
	}
	
	public String getColorHexStr ()
	{
		return m_strColorHex;
	}
	
	public int getColorValue ()
	{
		return m_nColorValue;
	}
	
	public int getID ()
	{
		return m_nID;
	}

	public static LogItemLevel getLevelFromCharID (char p_chID)
	{
		if (p_chID == 'V')
			return LogItemLevel.V;
		else if (p_chID == 'D')
			return LogItemLevel.D;
		else if (p_chID == 'I')
			return LogItemLevel.I;
		else if (p_chID == 'W')
			return LogItemLevel.W;
		else if (p_chID == 'E')
			return LogItemLevel.E;
		else
			return null;
	}
}
