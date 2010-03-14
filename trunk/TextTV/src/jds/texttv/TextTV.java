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
import android.graphics.Color;
import android.os.Bundle;
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

public class TextTV extends Activity implements OnGestureListener {
	public static final String TAG = "TextTV";
	private String header;
	private ProgressDialog waitingfordata;
	private Animation SlidOutAnimation;
	private Animation SlidInAnimation;
	private int CurrentPageNumber = 0;
	private String CurrentPage = null;
	private boolean WaitingForPage = false;
	private List<PageCach> PageCachArray = new ArrayList<PageCach>();
	
	private static final int MENU_EXIT = 0;
	private static final int MENU_ABOUT = 1;
	private static final int MENU_CONFIG = 2;
		
	private GestureDetector gestureScanner; 
	private boolean Port = true;
	private int DimX, DimY;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove the title
        requestWindowFeature  (Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
        WebView WV = (WebView) findViewById(R.id.MainWebView);
        WV.getSettings().setBuiltInZoomControls(true);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
        SlidOutAnimation = AnimationUtils.loadAnimation(this, R.anim.slideoutfromright);
        SlidInAnimation = AnimationUtils.loadAnimation(this, R.anim.slideinfromright);
        
        gestureScanner = new GestureDetector(this); 
        
        Button LoadButton = (Button) findViewById(R.id.LoadButton);        
        LoadButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (!WaitingForPage)
        		{
	        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
	        		int NewPage = 0;
					try
					{							
					NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
					}	
					catch (NumberFormatException e)
					{
					return;	
					}
	        		int Page = NewPage;	        		
	        		CurrentPageNumber = Page;
	        		
	        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);	        		
	        		
	        		RequestNewPage(Page);
        		}
        	}
		});
        
        ImageButton NextButton = (ImageButton) findViewById(R.id.NextButton);        
        NextButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	
        		if (!WaitingForPage)
        		{
	        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
	        		int NewPage = 0;
					try
					{							
					NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
					}	
					catch (NumberFormatException e)
					{
					return;	
					}
	        		int Page = NewPage;
	        		
	        		if (Page < 999)
	        		{
	        			Page++;
	        			PageNumber.setText(String.valueOf(Page));
	        			CurrentPageNumber = Page;
	        		}
	        		
	        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
	        		        		
	        		//WebView WV = (WebView) findViewById(R.id.MainWebView);
	        		//WV.startAnimation(SlidOutAnimation);        			        			        	
	        		
	        		RequestNewPage(Page);
        		}
        	}
		});
        
        ImageButton PrevButton = (ImageButton) findViewById(R.id.PrevButton);        
        PrevButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		if (!WaitingForPage)
        		{
	        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
	        		int NewPage = 0;
					try
					{							
					NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
					}	
					catch (NumberFormatException e)
					{
					return;	
					}
	        		int Page = NewPage;	        		
	        		
	        		if (Page > 100)
	        		{
	        			Page--;
	        			PageNumber.setText(String.valueOf(Page));
	        			CurrentPageNumber = Page;
	        		}
	        			
	        		
	        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
	        		
	        		//WebView WV = (WebView) findViewById(R.id.MainWebView);
	        		//WV.startAnimation(SlidOutAnimation);
	        		
	        		RequestNewPage(Page);
        		}
    		
        	}
		});
        
        EditText PageText = (EditText) findViewById(R.id.PageNumber);
        PageText.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	        		        		
        		((EditText)v).setText("");
        	}
        	        	
		});
        
        PageText.setOnKeyListener(new View.OnKeyListener() {					
			public boolean onKey(View v, int keyCode, KeyEvent event) {				
				if ((keyCode == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN))
				{
					if (!WaitingForPage)
	        		{
		        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
		        		int NewPage = 0;
						try
						{							
						NewPage = Integer.parseInt(PageNumber.getEditableText().toString());
						}	
						catch (NumberFormatException e)
						{
						return false;	
						}
		        		int Page = NewPage;		        		
		        		
		        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
		        		
		        		//WebView WV = (WebView) findViewById(R.id.MainWebView);
		        		//WV.startAnimation(SlidOutAnimation);
		        		
		        		RequestNewPage(Page);
	        		}
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
	    		
	    		RequestNewPage(NewPage);
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
	  		} catch (Exception e) {
	  			Log.e(TAG, "Problem restroting saved data", e);
	  		}
		}	
		
		//Check if the old page is still in memory
		if ((CurrentPage != null) && (CurrentPageNumber > 0))
		{
			InsertNewPage(CurrentPage, CurrentPageNumber);
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
			RequestNewPage(tempint);
		}
        
    }
    
    @Override
	public void onSaveInstanceState(Bundle savedInstanceState) {		
		savedInstanceState.putInt("CurrentPageNumber", CurrentPageNumber);				
		savedInstanceState.putString("CurrentPage", CurrentPage);
		Log.d(TAG,"onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
	}
    
    public void InsertNewPage(String PageData, int Page)
    {
    	if (waitingfordata != null)
  		  waitingfordata.dismiss();    	    
    	
    	WebView WV = (WebView) findViewById(R.id.MainWebView);
    	
    	WV.loadDataWithBaseURL (null, header+PageData, "text/html", "ISO-8859-1","about:blank");
    	CurrentPage = PageData;
    	WaitingForPage = false;
    	    		
    }

	@Override
	protected void onDestroy() {
		if (waitingfordata != null)
	  		  waitingfordata.dismiss();    	
		
		super.onDestroy();
	}
    
	private void RequestNewPage(int PageNumber)
	{
		DownlodPageThread DownlodThread = new DownlodPageThread(TextTV.this,PageNumber);        		
		DownlodThread.start();
		waitingfordata = ProgressDialog.show(TextTV.this,"TextTV","Laddar");
		WaitingForPage = true;
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
		
		 @Override  
		 public boolean onTouchEvent(MotionEvent me) {
			 gestureScanner.onTouchEvent(me);
			 return super.onTouchEvent(me); 
		           
		     }  
		
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	    {
		 //Log.d(TAG,"Scroll: X: " + distanceX + " Y: " + distanceY);
			/*
		if (!WaitingForPage)
		{
			if ((distanceX < -30) && (e2.getX() > (DimX / 2)))
			{
				//Scroll right
				Log.d(TAG,"Scroll right (Dist = " + distanceX + " Coord = " + e2.getX());
			}
			else if ((distanceX > 30) && (e2.getX() < (DimX / 2)))
			{
				//Scroll left
				Log.d(TAG,"Scroll left (Dist = " + distanceX + " Coord = " + e2.getX());
			}
		}
	     //return true;
	      
	      */
		return false;
	    }

		@Override
		public boolean onDown(MotionEvent e) {
			//Log.d("TAG","Down");
			return false;
			//return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			// TODO Auto-generated method stub
			//Log.d("TAG","Fling");
			return false;
			//return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onShowPress(MotionEvent e) {
			// TODO Auto-generated method stub		
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			// TODO Auto-generated method stub
			return false;
		}
		
		
		/*
		@Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
			WebView WV = (WebView) findViewById(R.id.MainWebView);
			
	        if ((keyCode == KeyEvent.KEYCODE_BACK) &&
	        	(WV.canGoBack())	
	            )
	        {	        	
	        	WV.goBack();	    
	        	Log.d(TAG,"Back to " + WV.getUrl());
	        	return true;	        	
	        }
	        return super.onKeyDown(keyCode, event);

	    } 
		*/
}