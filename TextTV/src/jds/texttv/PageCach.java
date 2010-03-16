package jds.texttv;

import java.io.Serializable;

import android.util.Log;

public class PageCach implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2010031200001L;	
	private String PageData;	
	private boolean InactivePage;
	private boolean Fetched;
	private int Page;
	private long TimeStamp;
	
	public PageCach(String PageData, int Page, boolean InactivePage, boolean Fetched) {
		super();
		this.PageData = PageData;
		this.InactivePage = InactivePage;
		this.Fetched = Fetched;
		this.Page = Page;
				
	}

	public String GetPageData()
	{
		return this.PageData;
	}
	
	public boolean GetInactivePage()
	{
		return this.InactivePage;
	}		
	
	public boolean GetFetched()
	{
		return this.Fetched;
	}
	
	public int GetPage()
	{
		return this.Page;
	}
	
	public void SetTimestmap()
	{
		this.TimeStamp = System.currentTimeMillis();
	}
	 
	public long GetTimestmap()
	{
		return TimeStamp;
	}
}