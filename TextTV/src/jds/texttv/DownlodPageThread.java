/**
  * This file is part of SR Player for Android
  *
  * SR Player for Android is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  *
  * SR Player for Android is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SR Player for Android.  If not, see <http://www.gnu.org/licenses/>.
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
	private int PageNumber; 
	private int RetryCount;
	private static boolean allreadyRunning = false;
	
	public DownlodPageThread(Activity activity, int Page) {
		this.activity = (TextTV) activity;
		this.PageNumber = Page;
	}
	
	@Override
	public void run() {
		if ( allreadyRunning ) {
			//this.activity.UpdateArray(null,null);			
			return;
		}
		allreadyRunning = true;
		URL url;		
        InputStream urlStream = null;
        String FeedUrl = "http://svt.se/svttext/tv/pages/" + PageNumber + ".html";
                
        for (RetryCount = 0; RetryCount < 3; RetryCount++)
        {
	        try {								
					url = new URL(FeedUrl);			
					urlStream = url.openStream();
					break;
				} catch (MalformedURLException e) {
					Log.e(TextTV.TAG, "Error getting page", e);
					
				} catch (IOException e) {
					Log.e(TextTV.TAG, "Error getting page", e);
					
				}
		
        }
        if (urlStream == null)
        {
        	allreadyRunning = false;
        	activity.InsertNewPage("Sidan kunde inte laddas");	        	
			return;
        }
        byte[] charbuffer = null;
        charbuffer = new byte[8096];
        
        try {
    		int Size = -1;
    		String pagestr = null;
    		
    		while ((Size = urlStream.read(charbuffer)) != -1)
            {                
                if (pagestr == null)
                {
                	//pagestr = new String(charbuffer,0,Size,"utf-8");
                	pagestr = new String(charbuffer,0,Size,"ISO-8859-1");                	
                	Log.d(TextTV.TAG,"Added " + Size + " bytes");
                }
                else
                {
                	//pagestr += new String(charbuffer,0,Size,"utf-8");
                	pagestr += new String(charbuffer,0,Size,"ISO-8859-1");
                	Log.d(TextTV.TAG,"Added another " + Size + " bytes");
                }
                
            }
	    		
    		if (pagestr == null)
    		{
    			allreadyRunning = false;
    			activity.InsertNewPage("Sidan kunde inte laddas");
    			return;
    		}
    		
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
    			Log.d(TextTV.TAG,"Texttv data for page " + PageNumber+ " found");    			
    			pagestr = pagestr.substring(PreStart, PreEnd+6);
    		}
    		else
    		{
    			Log.d(TextTV.TAG,"Could not find data for page " + PageNumber+ ". Start = " + PreStart + " End = " + PreEnd);
    			allreadyRunning = false;
    			activity.InsertNewPage("Sidan kunde inte laddas");
    			return;
    		}
    		
    		pagestr = pagestr.replaceAll("\r\n\r\n", "\r\n");
    		pagestr = pagestr.replaceAll("\r\n", "<br>");		
    		pagestr = pagestr.replaceAll("\n\n", "\n");
    		pagestr = pagestr.replaceAll("\n", "<br>");
    		pagestr = pagestr.replaceAll("../../images","http://svt.se/svttext/images");
    		pagestr += "</body></html>";
    		
    		/*
    		try {
    			pagestr = URLEncoder.encode(pagestr,"UTF-8");
    		} catch (UnsupportedEncodingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		*/    		
    		activity.InsertNewPage(pagestr);
        	
	        //this.activity.UpdateArray(PodInfo);
		} catch (IOException e) {
			Log.e(TextTV.TAG, "Error getting page", e);
			//this.activity.UpdateArray(null);
		} finally {
			try {
				if ( urlStream != null ) {					
					urlStream.close();		
					//Log.d(TextTV.TAG, "Page updare clode");
				}
			} catch (IOException e) { }
		}
		
		
		allreadyRunning = false;			
	}
		
}