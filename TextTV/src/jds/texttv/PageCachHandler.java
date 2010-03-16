package jds.texttv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class PageCachHandler {
	private List<PageCach> PageCachArray = new ArrayList<PageCach>();
	
	public int AddCachItem(PageCach NewCachItem) {
		//Check if the item is in the
		//cach already
		int Index = FindPageInCach(NewCachItem.GetPage());
		NewCachItem.SetTimestmap();
		if (Index >= 0)
		{			
			PageCachArray.set(Index, NewCachItem);
			//Log.d(TextTV.TAG,"Updating item. Timestamp = " + PageCachArray.get(Index).GetTimestmap());
			return Index;
		}
		else
		{
			PageCachArray.add(NewCachItem);
			//Log.d(TextTV.TAG,"Adding item. Timestamp = " + NewCachItem.GetTimestmap());
			return PageCachArray.size()-1;
		}
			
	}
	
	private int FindPageInCach(int PageNumber)
	{
		int i = 0;
		for (i=0; i<PageCachArray.size(); i++)
		{
			if (PageCachArray.get(i).GetPage() == PageNumber)
			{
				//Found the item, verify that it is not
				//older than 10 minutes			
				return i;
			}
		}
		return -1; //page not in cach
	}
	
	public int PageInCach(int PageNumber)
	{
		int Index = FindPageInCach(PageNumber);
		if (Index >= 0)
		{
			long CurrentTime = System.currentTimeMillis();
			if (Math.abs(CurrentTime)-PageCachArray.get(Index).GetTimestmap() < (10*1000*60))
			{
				return Index; //page in cach, return it's index
			}
			else
			{
				return -1; //Too old, need to be fetched again
			}
		}
		else
		{
			return Index;
		}
	}
	
	public PageCach GetCachItem(int Index)
	{		
		return PageCachArray.get(Index);
	}
	
	public int[] GetCachList(int StartPage, boolean ForceStartpageReload)
	{		
		int[] ReqList = new int[11]; //Max size
		int CurrIndex = 0;
		int CurrPage;
		int Count =0;
		
		if ((ForceStartpageReload) || (PageInCach(StartPage) < 0))
		{
			Count++;
			ReqList[CurrIndex++] = StartPage;
		}
		//ReqList[0] = StartPage; //First page is the start page
		
		//Check 5 pages before and 5 pages after 
		//and check which of the that is not in the 
		//cach
		for (CurrPage=StartPage+1;CurrPage < StartPage+6; CurrPage++)
		{
			if (CurrPage > 999)
				break;
			if (PageInCach(CurrPage) < 0)
			{
				Count++;
				ReqList[CurrIndex++] = CurrPage;
			}
		}
		for (CurrPage=StartPage-1;CurrPage > StartPage-6; CurrPage--)
		{
			if (CurrPage < 100)
				break;
			if (PageInCach(CurrPage) < 0)
			{
				Count++;
				ReqList[CurrIndex++] = CurrPage;
			}
		}
		int[] EndReqList = new int[Count]; //Final size
		for(CurrIndex =0; CurrIndex< Count; CurrIndex++)
		{
			EndReqList[CurrIndex] = ReqList[CurrIndex];
		}
		//Log.d(TextTV.TAG,"New reqlist with the size " + EndReqList.length);
				
		return ReqList;
	}
}