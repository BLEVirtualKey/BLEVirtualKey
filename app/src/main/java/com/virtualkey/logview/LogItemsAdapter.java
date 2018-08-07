package com.virtualkey.logview;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


public class LogItemsAdapter extends ArrayAdapter<LogItem>
{
	private Context			m_context;
	private List<LogItem> 	m_listItems;

	public LogItemsAdapter (Context p_context, List<LogItem> p_listItems)
    {
        // The textViewResourceId resource ID is set to 0, because not used.
		// Instead the getView method has been overridden
		super (p_context, 0, p_listItems);
         
        this.m_context = p_context;
        this.m_listItems = p_listItems;
     }

	@Override
	public View getView (int p_nPos, View p_convertView, ViewGroup parent)
	{
		LogItem item = m_listItems.get (p_nPos);
		LinearLayout layoutRow = null;
		TextView textview = null;
		
		if (p_convertView == null)
		{
			// Create a new layout to add the row textview
			layoutRow = new LinearLayout (this.m_context);
			layoutRow.setLayoutParams (new AbsListView.LayoutParams (
												AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT)
									  );
 
            textview = new TextView (this.m_context);
            layoutRow.addView (textview);
		} 
		else
		{
			// Reuse the already created view (a linear layout view) 
			layoutRow = (LinearLayout)p_convertView;
			textview = (TextView)layoutRow.getChildAt (0); 		// The layout contains only one TextView
		}

		textview.setText (item.getMsg ());
		textview.setTextColor (item.getLevel().getColorValue ());

		return layoutRow;
	}

	public void remove (int p_nPos)
	{
		LogItem item = m_listItems.get (p_nPos);
		remove (item);
	}

	public LogItem get (int p_nPos)
	{
		return m_listItems.get (p_nPos);
	}
	
	public List<LogItem> getEntries ()
	{
		return Collections.unmodifiableList (m_listItems);
	}
}
