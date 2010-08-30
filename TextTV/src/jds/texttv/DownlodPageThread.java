/**
  * This file is part of TextTV for Android
  *
  * TextTV for Android is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  *
  * TextTV for Android is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TextTV for Android.  If not, see <http://www.gnu.org/licenses/>.
  */
package jds.texttv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import jds.texttv.TextTV;

import android.app.Activity;
import android.util.Log;

public class DownlodPageThread extends Thread {
	private TextTV activity;	
	private int PageNumber[]; 
	private int RetryCount;
	private static boolean allreadyRunning = false;
	private static final String SvtOfflineStr = "Sidan ej i ";
	private boolean OnlinePageFound;	
	private int Direction;
	private boolean PageIsOffline = false;
	//String pagestr = null;
	int CurrPageNumber;
	
	final static int CHANNEL_SVT = 0;
	final static int CHANNEL_TV3 = 1;
	final static int CHANNEL_TV4 = 2;
	private int CurrentChannel;
	
	final static String SVT_URL = "http://svt.se/svttext/tv/pages/";
	final static String TV3_URL = "http://texttv.tv3.se/texttv/";
	final static String TV4_URL = "";
		
	public DownlodPageThread(Activity activity, int Page[], int Direction) {
		this.activity = (TextTV) activity;
		this.PageNumber = Page.clone();
		this.Direction = Direction;
		this.CurrentChannel = DownlodPageThread.CHANNEL_SVT;
		//this.CurrentChannel = DownlodPageThread.CHANNEL_TV3;
	}
	
	@Override
	public void run() {
		if ( allreadyRunning ) {			
			Log.d(TextTV.TAG,"Already running download thread");
			activity.InsertNewPage("Busy", 0, false,false);
			return;
		}
		allreadyRunning = true;
		
		Log.d(TextTV.TAG,"Running download thread");
		
		int PageListIndex = 0;
		PageIsOffline = false;
		OnlinePageFound = false;
		CurrPageNumber = 0;
		int MaxPageNum = 0;
		int MinPageNum = 999;
		//for (PageListIndex = 0; PageListIndex<PageNumber.length; PageListIndex++)
		while (true)
		{
			if (PageListIndex >= PageNumber.length)
			{
				//Check if an active page was found				
				if (OnlinePageFound)
				{
					break;
				}
				else
				{
					//No active page found
					//Continue searching until online page is found
					//But only if next/prev
					if (Direction != TextTV.DIR_NONE)
					{
						if (Direction == TextTV.DIR_NEXT)
						{
							CurrPageNumber = MaxPageNum++;
						}
						else
						{
							CurrPageNumber = MinPageNum--;
						}
					}
					else
					{
						break;
					}
				}
				
				
				//break;
			}	
			else
			{
				CurrPageNumber = PageNumber[PageListIndex];
			}
			
			if (PageListIndex > 40)
				break;
			
			if (MaxPageNum < CurrPageNumber)
				MaxPageNum = CurrPageNumber;
			
			if (MinPageNum > CurrPageNumber)
				MinPageNum = CurrPageNumber;
			
			//Log.d(TextTV.TAG,"CurrentPageNumber = " +  CurrPageNumber);
			
			if (CurrPageNumber < 100) {			
			activity.InsertNewPage("Invalid", 0, false,false);			
			PageListIndex++;
			continue;
			//break;
			};
			
			URL url;		
	        InputStream urlStream = null;
	        //String FeedUrl = "http://svt.se/svttext/tv/pages/" + CurrPageNumber + ".html";
	        String FeedUrl = GetChannelURL();
	                        
	        for (RetryCount = 0; RetryCount < 3; RetryCount++)
	        {
		        try {								
						url = new URL(FeedUrl);			
						urlStream = url.openStream();
						break;
					} catch (MalformedURLException e) {
						urlStream = null;
						Log.e(TextTV.TAG, "Error getting page", e);					
					} catch (IOException e) {
						urlStream = null;
						Log.e(TextTV.TAG, "Error getting page", e);					
					}
			
	        }
	        if (urlStream == null)
	        {
	        	allreadyRunning = false;	        	
	        	activity.InsertNewPage("Sidan kunde inte laddas", 0, false,false);	        	
				return;
	        }
	        byte[] charbuffer = null;
	        charbuffer = new byte[8096];
	        String pagestr = null;
	        try {
	    		int Size = -1;	    		
	    		
	    		while ((Size = urlStream.read(charbuffer)) != -1)
	            {                
	                if (pagestr == null)
	                {
	                	//pagestr = new String(charbuffer,0,Size,"utf-8");
	                	pagestr = new String(charbuffer,0,Size,"ISO-8859-1");                	
	                	//Log.d(TextTV.TAG,"Added " + Size + " bytes");
	                }
	                else
	                {
	                	//pagestr += new String(charbuffer,0,Size,"utf-8");
	                	pagestr += new String(charbuffer,0,Size,"ISO-8859-1");
	                	//Log.d(TextTV.TAG,"Added another " + Size + " bytes");
	                }
	                
	            }
		    		
	    		if (pagestr == null)
	    		{
	    			allreadyRunning = false;
	    			activity.InsertNewPage("Sidan kunde inte laddas",0, false,false);
	    			return;
	    		}
	    		
	    		/*
	    		//Extract the text
	    		int PreStart = 0;
	    		int PreEnd = 0;
	    		//Find the first pre tag
	    		PreStart = pagestr.indexOf("<pre");
	    		//Find the last pre tag    		
	    		//PreEnd = pagestr.indexOf("</pre>");
	    		PreEnd = pagestr.lastIndexOf("</pre>");
	    		if ((PreStart >= 0) && (PreEnd >= 0))
	    		{
	    			Log.d(TextTV.TAG,"Texttv data for page " + CurrPageNumber+ " found");    			
	    			pagestr = pagestr.substring(PreStart, PreEnd+6);
	    		}
	    		else
	    		{
	    			Log.d(TextTV.TAG,"Could not find data for page " + CurrPageNumber+ ". Start = " + PreStart + " End = " + PreEnd);
	    			allreadyRunning = false;
	    			activity.InsertNewPage("Sidan kunde inte laddas",0, false,false);
	    			return;
	    		}
	    		
	    		pagestr = pagestr.replaceAll("\r\n\r\n", "\r\n");
	    		pagestr = pagestr.replaceAll("\r\n", "<br>");		
	    		pagestr = pagestr.replaceAll("\n\n", "\n");
	    		pagestr = pagestr.replaceAll("\n", "<br>");
	    		pagestr = pagestr.replaceAll("../../images","http://svt.se/svttext/images");
	    		pagestr += "</body></html>";
	    		
	    		//Check if the page is offline
	    		if ((pagestr.length() < 100) && (pagestr.indexOf(SvtOfflineStr) >= 0))
	    		{	    				    			
	    			PageIsOffline = true;
	    			
	    		}
	    		else
	    		{
	    			PageIsOffline = false;
	    			OnlinePageFound = true;
	    		}
	    					    		    		    	
	    		activity.InsertNewPage(pagestr, CurrPageNumber, PageIsOffline, (Direction == TextTV.DIR_NONE));
	    		*/
	    		ParsePageStr(pagestr);
	        	
		        //this.activity.UpdateArray(PodInfo);
			} catch (IOException e) {
				Log.e(TextTV.TAG, "Error getting page", e);
				activity.InsertNewPage("Error", 0, false,false);
			} finally {
				try {
					if ( urlStream != null ) {					
						urlStream.close();							
					}
				} catch (IOException e) { }
			}
			
			PageListIndex++;
		}
		
		Log.d(TextTV.TAG,"DownloadPageThread completed with no errors");
		allreadyRunning = false;			
	}
	
	private String GetChannelURL()
	{
		switch (this.CurrentChannel)
		{
		case CHANNEL_SVT :
			return SVT_URL + this.CurrPageNumber + ".html";			
		case CHANNEL_TV3 :
			return TV3_URL + this.CurrPageNumber + "-01.htm";
		case CHANNEL_TV4 :
			break;
		default : 
			return "";
		}
		
		return "";
	}
		
	private void ParsePageStr(String PageStringToParse)
	{
		switch (this.CurrentChannel)
		{
		case CHANNEL_SVT:
			//Extract the text
			int PreStart = 0;
			int PreEnd = 0;
			//Find the first pre tag
			PreStart = PageStringToParse.indexOf("<pre");
			//Find the last pre tag    		
			//PreEnd = pagestr.indexOf("</pre>");
			PreEnd = PageStringToParse.lastIndexOf("</pre>");
			
			if ((PreStart >= 0) && (PreEnd >= 0))
			{			    		
				PageStringToParse = PageStringToParse.substring(PreStart, PreEnd+6);
			}
			else
			{			
				allreadyRunning = false;
				activity.InsertNewPage("Sidan kunde inte laddas",0, false,false);
				return;
			}				
			
			PageStringToParse = PageStringToParse.replaceAll("\r\n\r\n", "\r\n");
			PageStringToParse = PageStringToParse.replaceAll("\r\n", "<br>");		
			PageStringToParse = PageStringToParse.replaceAll("\n\n", "\n");
			PageStringToParse = PageStringToParse.replaceAll("\n", "<br>");
			PageStringToParse = PageStringToParse.replaceAll("../../images","http://svt.se/svttext/images");
			PageStringToParse += "</body></html>";
			break;
		case CHANNEL_TV3:
			int MapStart = 0;
			int MapEnd = 0;
			//Find the first map tag
			MapStart = PageStringToParse.indexOf("<map");
			//Find the map end tag    					
			MapEnd = PageStringToParse.indexOf("</map>");
			
			if ((MapStart >= 0) && (MapEnd >= 0))
			{			    		
				PageStringToParse = PageStringToParse.substring(MapStart, MapEnd+6);
			}
			else
			{
				PageStringToParse = ""; //no map found. Consider it as offline
				break;
			}
			PageStringToParse += "</td></tr></table></body></html>";
			String Header = "";
			Header = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">";  
			Header += "<title></title></head><body bgcolor=\"#000000\" topmargin=\"0\" leftmargin=\"0\" marginwidth=\"0\" marginheight=\"0\">"; 
			Header += "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"400\" height=\"360\">";  
			Header += "<tr><td width=\"400\" height=\"300\" align=\"left\" valign=\"top\" rowspan=\"3\">";
			Header += "<img src=\"http://texttv.tv3.se/texttv/images/" + this.CurrPageNumber + "-01.gif\""; 
			//Header += " height=300 width=400 usemap=\"#" + this.CurrPageNumber + "-01\" border=0>";
			Header += " height=300 width=320 usemap=\"#" + this.CurrPageNumber + "-01\" border=0>";
			
			PageStringToParse = Header + PageStringToParse;
			
			break;
		case CHANNEL_TV4:
			break;
		}
		 							
		//Check if the page is offline
		if ( ((PageStringToParse.length() < 100) && (PageStringToParse.indexOf(SvtOfflineStr) >= 0))
				||
				(PageStringToParse.length() == 0)
			)
				
		{	    				    			
			PageIsOffline = true;
			
		}
		else
		{
			PageIsOffline = false;
			OnlinePageFound = true;
		}
		
		activity.InsertNewPage(PageStringToParse, CurrPageNumber, PageIsOffline, (Direction == TextTV.DIR_NONE));
	}
}
