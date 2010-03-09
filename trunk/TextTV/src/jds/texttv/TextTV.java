package jds.texttv;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class TextTV extends Activity {
	public static final String TAG = "TextTV";
	private String header;
	private ProgressDialog waitingfordata;
	private Animation SlidOutAnimation;
	private Animation SlidInAnimation;
	
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
        
        
        Button LoadButton = (Button) findViewById(R.id.LoadButton);        
        LoadButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	        		        		
        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
        		int Page = Integer.parseInt(PageNumber.getEditableText().toString());
        		
        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
        		
        		DownlodPageThread DownlodThread = new DownlodPageThread(TextTV.this,Page);        		
        		DownlodThread.start();
        		waitingfordata = ProgressDialog.show(TextTV.this,"TextTV","Laddar");                
    		
        	}
		});
        
        ImageButton NextButton = (ImageButton) findViewById(R.id.NextButton);        
        NextButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	        		        		
        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
        		int Page = Integer.parseInt(PageNumber.getEditableText().toString());
        		
        		if (Page < 999)
        		{
        			Page++;
        			PageNumber.setText(String.valueOf(Page));
        		}
        		
        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
        		        		
        		//WebView WV = (WebView) findViewById(R.id.MainWebView);
        		//WV.startAnimation(SlidOutAnimation);        		
        		
        		DownlodPageThread DownlodThread = new DownlodPageThread(TextTV.this,Page);        		
        		DownlodThread.start();
        		waitingfordata = ProgressDialog.show(TextTV.this,"TextTV","Laddar");                
    		
        	}
		});
        
        ImageButton PrevButton = (ImageButton) findViewById(R.id.PrevButton);        
        PrevButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	        		        		
        		EditText PageNumber = (EditText) findViewById(R.id.PageNumber);
        		int Page = Integer.parseInt(PageNumber.getEditableText().toString());
        		
        		if (Page > 100)
        		{
        			Page--;
        			PageNumber.setText(String.valueOf(Page));
        		}
        			
        		
        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        		imm.hideSoftInputFromWindow(PageNumber.getWindowToken(), 0);
        		
        		//WebView WV = (WebView) findViewById(R.id.MainWebView);
        		//WV.startAnimation(SlidOutAnimation);
        		
        		DownlodPageThread DownlodThread = new DownlodPageThread(TextTV.this,Page);        		
        		DownlodThread.start();
        		waitingfordata = ProgressDialog.show(TextTV.this,"TextTV","Laddar");                
    		
        	}
		});
        
        EditText PageText = (EditText) findViewById(R.id.PageNumber);
        PageText.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {	        		        		
        		((EditText)v).setText("");
        	}
		});
        
        Configuration c = getResources().getConfiguration();
        if(c.orientation == Configuration.ORIENTATION_PORTRAIT ) 
        {
        	DisplayMetrics dm = new DisplayMetrics();
            this.getWindowManager().getDefaultDisplay().getMetrics(dm);
            //int height = dm.heightPixels;
            int width = dm.widthPixels; 
                            
            if (width < 440)
            {
            	//Scale the WebView
            	double Scaling = (100*(((double)width)/440));
            	Log.d("TextTV", "Scaling to " + Scaling + "percent");
            	WV.setInitialScale((int)Scaling);
            }
        }
        
        
        WV.setBackgroundColor(Color.BLACK);
        
        InputStream  HeaderPageStream = getResources().openRawResource(R.raw.header);
        byte[] charbuffer = null;
        int Size=0;
        charbuffer = new byte[65000];
        try {
			Size = HeaderPageStream.read(charbuffer, 0, 65000);
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
        
        DownlodPageThread DownlodThread = new DownlodPageThread(this,100);
        DownlodThread.start();
       waitingfordata = ProgressDialog.show(TextTV.this,"TextTV","Laddar");
        
    }
    
    public void InsertNewPage(String PageData)
    {
    	if (waitingfordata != null)
  		  waitingfordata.dismiss();    	    
    	
    	WebView WV = (WebView) findViewById(R.id.MainWebView);
    	//WV.loadData(header+PageData, "text/html", "utf-8"); 
    	//WV.loadData(header+PageData, "text/html", "ISO-8859-1");
    	
    	WV.loadDataWithBaseURL (null, header+PageData, "text/html", "ISO-8859-1","about:blank");
    	
		//WV.startAnimation(SlidInAnimation);
    }
}