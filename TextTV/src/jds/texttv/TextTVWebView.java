package jds.texttv;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.webkit.WebView;
 

public class TextTVWebView extends WebView implements OnGestureListener{	
	private GestureDetector gestureScanner = new GestureDetector(this);
	private TextTV Parent = null;
	private boolean NewPage = false;
	
	public TextTVWebView(Context context) 
	{
		super(context);		
	}

	public TextTVWebView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
	
	public TextTVWebView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	@Override  
	public boolean onTouchEvent(MotionEvent me) {		 
		//super.onTouchEvent(me);		
		super.onTouchEvent(me);
		
		gestureScanner.onTouchEvent(me);
		
		
		
		
		/*
		if ((me.getAction() == MotionEvent.ACTION_DOWN) ||
			(me.getAction() == MotionEvent.ACTION_UP) ||
			(me.getAction() == MotionEvent.ACTION_MOVE)
			)
		{			
			super.onTouchEvent(me);
		}			
		*/
		
		
		return true;
		
		
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub		
		return true;		
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {		
		if ((Parent != null) && (Math.abs(velocityY) < Math.abs(velocityX)))
		{				
			if (velocityX > 200)
			{
				//Fling scroll left 
				Parent.PrevPage();
				//this.scrollTo(0,0);
				NewPage = true;
			}
			else if (velocityX < -200)
			{
				//Fling scroll right
				
				Parent.NextPage();
				NewPage = true;
				//this.scrollTo(0,0);				
				/*
				Message m = new Message();
		        m.what = TextTV.MESSAGE_NEXT;		        
		        Parent.viewUpdateHandler.sendMessage(m);
		        */
			}
		}
		
		//this.flingScroll((int)(e1.getX()-e2.getX())*50, (int)(e1.getY()-e2.getY())*50);
		
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		//this.flingScroll((int)distanceX*80, (int)distanceY*80);
		//if (distanceY > 10)
		//	this.scrollBy((int)distanceX, (int)distanceY);
		
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		invokeZoomPicker();
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub		
		//return false;
		return true;
	}
	
	public void SetParent(TextTV Parent)
	{
		this.Parent = Parent;
	}
}
