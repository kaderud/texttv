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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TextTV extends Activity {
	public static final String TAG = "TextTV";
	private String header;
	private ProgressDialog waitingfordata;
	private Animation SlidInFromRigthAnimation;
	private Animation SlidInFromLeftAnimation;
	private int CurrentPageNumber = 0;
	private String CurrentPage = null;
	private boolean WaitingForPage = false;
	private boolean WaitingForOnlinePage = false;
	private boolean FavSwitchMode = false;
	private static PageCachHandler PageCachHandler = new PageCachHandler(); 
	
	private static final int MESSAGE_NEWPAGE = 0;
	private static final int MESSAGE_ERROR = 1;
		
	private static final int MENU_EXIT = 0;
	private static final int MENU_ABOUT = 1;
	private static final int MENU_CONFIG = 2;
	private static final int MENU_FAVORITES = 3;
	
	public static final int DIR_NONE = 0;
	public static final int DIR_NEXT = 1;
	public static final int DIR_PREV = 2;
			
	private boolean Port = true;
	private int DimX, DimY;
	
	private TextTVDBAdapter TextTVDB;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove the title
        requestWindowFeature  (Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        TextTVDB = new TextTVDBAdapter(this);
		TextTVDB.open();		
		
        
        WebView WV = (WebView) findViewById(R.id.MainWebView);
        WV.getSettings().setBuiltInZoomControls(true);
        ((TextTVWebView)WV).SetParent(this);
               
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
        SlidInFromRigthAnimation = AnimationUtils.loadAnimation(this, R.anim.slideinfromright);
        SlidInFromLeftAnimation = AnimationUtils.loadAnimation(this, R.anim.slideinfromleft);
                        
        Button LoadButton = (Button) findViewById(R.id.LoadButton);        
        LoadButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		LoadPage();        		
        	}
		});
        
        ImageButton NextButton = (ImageButton) findViewById(R.id.NextButton);        
        NextButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	
        		NextPage();        		
        	}
		});
        
        ImageButton PrevButton = (ImageButton) findViewById(R.id.PrevButton);        
        PrevButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		PrevPage();    		
        	}
		});
        
        EditText PageText = (EditText) findViewById(R.id.PageNumber);
        PageText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
				{
					((EditText)v).setText("");
				}
			}
		});
        PageText.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	        		        		
        		((EditText)v).setText("");
        	}
        	        	
		});
        
        ImageButton FavButton = (ImageButton) findViewById(R.id.FavButton);        
        FavButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	
        		Cursor DBCursor;
        		DBCursor = TextTVDB.fetchFavorite(CurrentPageNumber);
        		ImageButton FavButton = (ImageButton) findViewById(R.id.FavButton);
        		Context context = getApplicationContext();		
       			int duration = Toast.LENGTH_LONG;
        		
        		if ((DBCursor != null) && (DBCursor.getCount() > 0))
        		{
        			//Remove if from the favorites
        			TextTVDB.deleteFavorite(CurrentPageNumber);
        			Toast toast = Toast.makeText(context, R.string.fav_delete, duration);
            		toast.show();
        		}
        		else
        		{        			
        			//Add it to the favorites
        			TextTVDB.createFavorite(CurrentPageNumber, "");
        			Toast toast = Toast.makeText(context, R.string.fav_add, duration);
        			toast.show();
        		}
        		DBCursor.close();
        		UpdateFavicon(CurrentPageNumber);
        	}
		});
        
        ImageButton FavSwitchButton = (ImageButton) findViewById(R.id.FavSwitchButton);        
        FavSwitchButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	
        		Cursor DBCursor;
        		DBCursor = TextTVDB.fetchAllFavorites();
        		ImageButton FavButton = (ImageButton) findViewById(R.id.FavButton);
        		Context context = getApplicationContext();		
       			int duration = Toast.LENGTH_LONG;
        		
        		if ((DBCursor != null) && (DBCursor.getCount() > 0))
        		{
        			if (FavSwitchMode)
        			{
        			Toast toast = Toast.makeText(context, R.string.favswitch_off, duration);
        			toast.show();
        			FavSwitchMode = false;
        			}
        			else
        			{
        			Toast toast = Toast.makeText(context, R.string.favswitch_on, duration);
        			toast.show();
        			FavSwitchMode = true;
        			}        			
        		}
        		else
        		{        			
        			        			
        			Toast toast = Toast.makeText(context, R.string.favswicth_no_avail, duration);
        			toast.show();
        		}
        		
        		if (DBCursor != null)
        			DBCursor.close();
        		
        		UpdateFavSwitchicon(FavSwitchMode);
        	}
		});
        
        PageText.setOnKeyListener(new View.OnKeyListener() {					
			public boolean onKey(View v, int keyCode, KeyEvent event) {				
				if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))
				{
					LoadPage();					
				}
				return false;
			}
		});
        
        Configuration c = getResources().getConfiguration();
        if(c.orientation == Configuration.ORIENTATION_PORTRAIT ) 
        {
        	Port = true;
        	DisplayMetrics dm = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            DimY = dm.heightPixels;
            DimX = dm.widthPixels; 
                            
        	//Scale the WebView
        	double Scaling = (100*(((double)DimX)/440));
        	Log.d("TextTV", "Scaling to " + Scaling + "percent");
        	WV.setInitialScale((int)Scaling);
        }
        else
        {
        	Port = false;
        }
        
        
        WV.setBackgroundColor(Color.BLACK);
        
        InputStream  HeaderPageStream = getResources().openRawResource(R.raw.header);
        byte[] charbuffer = null;
        int Size=0;
        charbuffer = new byte[5000];
        try {
			Size = HeaderPageStream.read(charbuffer, 0, 5000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		header = null;

		try {
			header = new String(charbuffer,0,Size,"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
								
        //WV.loadData(header+pagestr, "text/html", "utf-8");
		
		WV.setWebViewClient(new WebViewClient() {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {			
			//Extract the requested pagenumber
			if (!WaitingForPage)
			{
				int NewPage = 0;
				try
				{							
				NewPage = Integer.parseInt(url.substring(0, 3));
				EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
				PageNumber.setText(String.valueOf(NewPage));
				}	
				catch (NumberFormatException e)
				{
				return true;	
				}
				CurrentPageNumber = NewPage;
	    		RequestNewPage(NewPage, false, DIR_NONE);
			}
			return true;
		}
		
		 });

        
		if ( savedInstanceState != null ) {
			try {
				String TempS;
				int TempInt;
				TempS = savedInstanceState.getString("CurrentPage");
				if (TempS != null)
				{
					CurrentPage = TempS;
				}
				TempInt = savedInstanceState.getInt("CurrentPageNumber", 0);
				if (TempInt > 0)
				{
					CurrentPageNumber = TempInt;
				}
				
				FavSwitchMode = savedInstanceState.getBoolean("FAVSWITCHMODE");
				
	  		} catch (Exception e) {
	  			Log.e(TAG, "Problem restroting saved data", e);
	  		}
		}	
		
		//Check if the old page is still in memory
		if ((CurrentPage != null) && (CurrentPageNumber > 0))
		{
			InsertNewPage(CurrentPage, CurrentPageNumber, false, true);
			PageNumber = (EditText) findViewById(R.id.PageNumber);
			PageNumber.setText(String.valueOf(CurrentPageNumber));
		}
		else
		{	
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String startPagePregStr;
			
			startPagePregStr = prefs.getString("startpage", "0");
			
			int tempint = 0;
			try {
				tempint = Integer.parseInt(startPagePregStr);
			} catch (NumberFormatException e) {
				tempint = 100;
			}
			if (tempint == 0)
				tempint = 100;
						
			
			PageNumber = (EditText) findViewById(R.id.PageNumber);
			PageNumber.setText(String.valueOf(tempint));
			CurrentPageNumber = tempint;
			RequestNewPage(tempint, true, DIR_NONE);
			
		}
		
		UpdateFavSwitchicon(FavSwitchMode);
        
    }
    
    @Override
	public void onSaveInstanceState(Bundle savedInstanceState) {		
		savedInstanceState.putInt("CurrentPageNumber", CurrentPageNumber);				
		savedInstanceState.putString("CurrentPage", CurrentPage);
		savedInstanceState.putBoolean("FAVSWITCHMODE", FavSwitchMode);
		Log.d(TAG,"onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
	}
    
    public void InsertNewPage(String PageData, int Page, boolean Offline, boolean ForceDisplay)
    {    	    
    	WebView WV = (WebView) findViewById(R.id.MainWebView);
    	
    	//Log.d(TAG, "InsertNewPage number: " + Page);    	
    	
    	if ((CurrentPageNumber == Page) || (WaitingForOnlinePage))
    	{    		    	
	    	if ((!Offline) || (ForceDisplay))
	    	{	
    		WV.loadDataWithBaseURL (null, header+PageData, "text/html", "ISO-8859-1","about:blank");    	
	    	CurrentPage = PageData;
	    		    	
	    	if (waitingfordata != null)
  	  		  waitingfordata.dismiss();
	    	WaitingForPage = false;
	    		    	
	    	Message m = new Message();
	        m.what = TextTV.MESSAGE_NEWPAGE;
	        m.getData().putInt("PAGE", Page);
	        TextTV.this.viewUpdateHandler.sendMessage(m);
	        
	        WaitingForOnlinePage = false;
	    	}
	    	else
	    	{
	    		WaitingForOnlinePage = true;
	    	}
    	}
    	
    	if (WaitingForPage && (Page == 0))
    	{
    		//Error page. Remove dialog so it will not 
    		//get stuck
    		if (waitingfordata != null)    		    		 
  	  		  waitingfordata.dismiss();
    		
    		
    		WaitingForPage = false;
    		    				
			Message m = new Message();
	        m.what = TextTV.MESSAGE_ERROR;
	        m.getData().putInt("PAGE", Page);
	        TextTV.this.viewUpdateHandler.sendMessage(m);
   			
    	}
    	
    	//Insert the page in the cach
    	if (Page >= 100)
    	{
    		PageCach NewCachItem = new PageCach(PageData, Page, Offline, true);    	
    		PageCachHandler.AddCachItem(NewCachItem);
    	}
    	    	
    }

	@Override
	protected void onDestroy() {
		if (waitingfordata != null)
	  		  waitingfordata.dismiss();    	
		
		TextTVDB.close();
		
		super.onDestroy();
	}
    
	private void RequestNewPage(int PageNumber, boolean ForceFetch, int Direction)
	{
		//check if the item is in the cach already		
		int CachIndex = PageCachHandler.PageInCach(PageNumber);
		PageCach PageInCach = null;
		int[] PageList;
		
		//Check if the cach is offline
		if ((!ForceFetch) && (CachIndex >= 0) && (Direction != TextTV.DIR_NONE))
		{
			PageInCach = PageCachHandler.GetCachItem(CachIndex);
			if (PageInCach.GetInactivePage())
			{
				//The requested page is inactive
				//Find the next active page
				int Page = PageNumber;				
				while (true)
				{				
					if (Direction == TextTV.DIR_NEXT)
					{
						Page++;
					}
					else
					{
						Page--;
					}
					if (Page > 999)
						Page = 100;
					
					if (Page < 100)
						Page = 100;
					
					CachIndex = PageCachHandler.PageInCach(Page);
					if (CachIndex < 0)
						break; //Page not in cach
					PageInCach = PageCachHandler.GetCachItem(CachIndex);
					if (!PageInCach.GetInactivePage())
						break; //Found an active page					
				}
				CurrentPageNumber = Page;	
								
				EditText PageNumberBox = (EditText) findViewById(R.id.PageNumber);
				PageNumberBox.setText(String.valueOf(Page));								
				
			}						
		}
		
		if ((!ForceFetch) && (CachIndex >= 0))
		{
			PageInCach = PageCachHandler.GetCachItem(CachIndex);			
			WebView WV = (WebView) findViewById(R.id.MainWebView);							
						
			WV.loadDataWithBaseURL (null, header+PageInCach.GetPageData(), "text/html", "ISO-8859-1","about:blank");
			if (Direction != TextTV.DIR_NONE)
			{
				if (Direction == TextTV.DIR_NEXT)
				{
					WV.startAnimation(SlidInFromRigthAnimation);
				}
				else
				{
					WV.startAnimation(SlidInFromLeftAnimation);
				}
			}
			
	    	CurrentPage = PageInCach.GetPageData();
	    	UpdateFavicon(PageNumber);
	    	
	    	if (!FavSwitchMode)
	    	{
	    		PageList = PageCachHandler.GetCachList(CurrentPageNumber, false);
	    	}
	    	else
	    	{
	    		PageList = GetFavoriteList(CurrentPageNumber, false);
	    	}
	    	
	    	if (PageList.length > 0)
	    	{
	    		//Prefetch new pages
	    		DownlodPageThread DownlodThread = new DownlodPageThread(TextTV.this,PageList,Direction);        		
				DownlodThread.start();
	    	}
		}
		else
		{					  			
			if (!FavSwitchMode)
	    	{
	    		PageList = PageCachHandler.GetCachList(CurrentPageNumber, ForceFetch);
	    	}
	    	else
	    	{
	    		PageList = GetFavoriteList(CurrentPageNumber, ForceFetch);
	    	}
			
			WaitingForPage = true;
			WaitingForOnlinePage = false;
			waitingfordata = ProgressDialog.show(TextTV.this,"TextTV","Laddar");
			DownlodPageThread DownlodThread = new DownlodPageThread(TextTV.this,PageList,Direction);        		
			DownlodThread.start();
			
			
		}
	}
	
	private void UpdateFavSwitchicon(boolean FavSwitchMode)
	{
		ImageButton FavSwitchButton = (ImageButton) findViewById(R.id.FavSwitchButton);
		if (FavSwitchMode)
		{
			FavSwitchButton.setImageResource(R.drawable.fav_switch_pressed);
		}
		else
		{
			FavSwitchButton.setImageResource(R.drawable.fav_switch);
		}
	}
	
	private void UpdateFavicon(int PageNumber) 
	{
		//Check if the page is in the DB
		Cursor DBCursor = null;
		DBCursor = TextTVDB.fetchFavorite(PageNumber);
		ImageButton FavButton = (ImageButton) findViewById(R.id.FavButton);
		if ((DBCursor != null) && (DBCursor.getCount() > 0))
		{
			FavButton.setImageResource(R.drawable.favorite_pressed);			
		}
		else
		{
			FavButton.setImageResource(R.drawable.favorite);
		}
		if (DBCursor != null)
			DBCursor.close();
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, "onCreateOptionsMenu");
		super.onCreateOptionsMenu(menu);
		menu.add(0, TextTV.MENU_EXIT, 0, R.string.menu_exit).
			setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		menu.add(0, TextTV.MENU_ABOUT, 0, R.string.menu_about).
			setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, TextTV.MENU_CONFIG, 0, R.string.menu_config).
			setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, TextTV.MENU_FAVORITES, 0, R.string.menu_favorites).
			setIcon(R.drawable.ic_menu_star);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Log.d(TAG, "onMenuItemSelected");
		switch (item.getItemId()) {
		case TextTV.MENU_EXIT:
			handleMenuExit();
			return true;
		case TextTV.MENU_ABOUT:
			handleMenuAbout();
			return true;
		case TextTV.MENU_CONFIG:
			handleMenuConfig();
			return true;
		case TextTV.MENU_FAVORITES:
			ShowFavoritesDialog();
			return true;
		
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
		
		private void handleMenuAbout() {		
			View view = View.inflate(this, R.layout.about, null);
			TextView textView = (TextView) view.findViewById(R.id.message);
			textView.setMovementMethod(LinkMovementMethod.getInstance());
			textView.setText(R.string.about_message);
			new AlertDialog.Builder(this)
					.setTitle(getResources().getText(R.string.about_title))		
			        .setView(view)
					.setPositiveButton(android.R.string.ok, null)
			        .setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// Do nothing...
								}
							})
					.show();
				
		}
			
		private void handleMenuExit() {
			this.finish();
		}
		
		private void handleMenuConfig() {		
			Intent launchIntent = new Intent(TextTV.this, TextTVPreferences.class);			
			TextTV.this.startActivity(launchIntent);
		}
		
		 		
	Handler viewUpdateHandler = new Handler(){
	        public void handleMessage(Message msg) {
	        	Context context = getApplicationContext();
	        	int duration = Toast.LENGTH_LONG;
	             switch (msg.what) {
	             case MESSAGE_NEWPAGE :
	            	 int NewPage = msg.getData().getInt("PAGE");
	            	 UpdateFavicon(NewPage);
	            	 
	            	 if (NewPage >= 100)
	            	 {
	            		 EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
	            		 PageNumber.setText(String.valueOf(NewPage));
	            	 }
	            	 break;
	             case MESSAGE_ERROR : 	            				        		
	        		Toast toast = Toast.makeText(context, R.string.error_str, duration);
	     			toast.show();
	     			break;
	             }
	        }
	};
	
	void ShowFavoritesDialog()
	{
		final Cursor FavDialogCursor = TextTVDB.fetchAllFavorites();		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);		
		builder.setTitle("Välj favorit");
		builder.setSingleChoiceItems(FavDialogCursor, -1, TextTVDBAdapter.KEY_PAGE, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        dialog.dismiss();
		        //Cursor SelFavCursor = TextTVDB.fetchAllFavorites();
		        FavDialogCursor.moveToPosition(item);
		        
		        EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
        		CurrentPageNumber = FavDialogCursor.getInt(TextTVDBAdapter.INDEX_PAGE);
        		PageNumber.setText(String.valueOf(CurrentPageNumber));
        		        		
		        RequestNewPage(CurrentPageNumber, false, DIR_NONE);
		        FavDialogCursor.close();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		

	}
	
	public int[] GetFavoriteList(int StartPage, boolean ForceStartpageReload)
	{		
		int[] ReqList = new int[11]; //Max size
		int CurrIndex = 0;
		int CurrPage;
		int Count =0;
		
		if ((ForceStartpageReload) || (PageCachHandler.PageInCach(StartPage) < 0))
		{
			Count++;
			ReqList[CurrIndex++] = StartPage;
		}
		
		//Check 5 pages before and 5 pages after 
		//and check which of the that is not in the 
		//cach
		Cursor FavCursor = TextTVDB.getNextFavorite(StartPage);
		int FavCount;		
		for (FavCount = 0; FavCount < FavCursor.getCount(); FavCount++)
		{			
			CurrPage = FavCursor.getInt(TextTVDBAdapter.INDEX_PAGE);
			if (PageCachHandler.PageInCach(CurrPage) < 0)
			{
				Count++;
				ReqList[CurrIndex++] = CurrPage;
			}
		}
		FavCursor.close();
		FavCursor = TextTVDB.getNextFavorite(StartPage);
		for (FavCount = 0; FavCount < FavCursor.getCount(); FavCount++)
		{
			CurrPage = FavCursor.getInt(TextTVDBAdapter.INDEX_PAGE);
			if (PageCachHandler.PageInCach(CurrPage) < 0)
			{
				Count++;
				ReqList[CurrIndex++] = CurrPage;
			}
		}
		FavCursor.close();
		
		int[] EndReqList = new int[Count]; //Final size
		for(CurrIndex =0; CurrIndex< Count; CurrIndex++)
		{
			EndReqList[CurrIndex] = ReqList[CurrIndex];
		}		
				
		return ReqList;
	}
	
	public void NextPage()
	{
		if (!WaitingForPage)
		{
			EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
			int Page = 100;
			
			//Check the mode
			if (FavSwitchMode)
			{
				Cursor FavCursor = TextTVDB.getNextFavorite(CurrentPageNumber);
				if ((FavCursor != null) && (FavCursor.getCount() > 0))
				{
				Page = FavCursor.getInt(TextTVDBAdapter.INDEX_PAGE);
				FavCursor.close();
				PageNumber.setText(String.valueOf(Page));
    			CurrentPageNumber = Page;
				}
				else
				{
					FavSwitchMode = false;
					UpdateFavSwitchicon(FavSwitchMode);
					
				}
			}
			else
			{		        		
        		int NewPage = 0;
				try
				{							
				NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
				}	
				catch (NumberFormatException e)
				{
				return;	
				}
        		Page = NewPage;
        		
        		if (Page < 999)
        		{
        			Page++;
        			PageNumber.setText(String.valueOf(Page));
        			CurrentPageNumber = Page;
        		}
			}
    		
    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
    		        		
    		//WebView WV = (WebView) findViewById(R.id.MainWebView);
    		//WV.startAnimation(SlidOutAnimation);        			        			        	
    		
    		RequestNewPage(Page, false, TextTV.DIR_NEXT);
		}
	}
	
	public void PrevPage()
	{
		if (!WaitingForPage)
		{
			EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
			int Page = 100;
			
			//Check the mode
			if (FavSwitchMode)
			{
				Cursor FavCursor = TextTVDB.getPrevFavorite(CurrentPageNumber);
				if ((FavCursor != null) && (FavCursor.getCount() > 0))
				{
				Page = FavCursor.getInt(TextTVDBAdapter.INDEX_PAGE);
				FavCursor.close();
				PageNumber.setText(String.valueOf(Page));
    			CurrentPageNumber = Page;
				}
				else
				{
					FavSwitchMode = false;
					UpdateFavSwitchicon(FavSwitchMode);
					
				}
			}
			else
			{		        		
        		int NewPage = 0;
				try
				{							
				NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
				}	
				catch (NumberFormatException e)
				{
				return;	
				}
        		Page = NewPage;
        		
        		if (Page > 100)
        		{
        			Page--;
        			PageNumber.setText(String.valueOf(Page));
        			CurrentPageNumber = Page;
        		}

			}
    		
    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
    		        		
    		//WebView WV = (WebView) findViewById(R.id.MainWebView);
    		//WV.startAnimation(SlidOutAnimation);        			        			        	
    		
    		RequestNewPage(Page, false, TextTV.DIR_PREV);
		}
	}
	
	public void LoadPage()
	{	
		if (!WaitingForPage)
		{
    		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
    		
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			String startPageStr;	    			
			startPageStr = prefs.getString("startpage", "100");
			int NewPage = 0;
			if (PageNumber.getEditableText().toString().length() == 0)
			{
				NewPage = Integer.parseInt(startPageStr);
				PageNumber.setText(String.valueOf(NewPage));
			}
			else
			{
        		
				try
				{							
				NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
				}	
				catch (NumberFormatException e)
				{
				return;	
			}
			}
    		int Page = NewPage;	        		
    		CurrentPageNumber = Page;
    		
    		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);	        		
    		
    		RequestNewPage(Page, true, DIR_NONE);
		}
	}

}