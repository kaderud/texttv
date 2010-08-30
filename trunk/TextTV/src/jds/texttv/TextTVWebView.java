package jds.texttv;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.webkit.WebView;
 

public class TextTVWebView extends WebView implements OnGestureListener{	
	private GestureDetector gestureScanner = new GestureDetector(this);
	private TextTV Parent = null;

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
		 gestureScanner.onTouchEvent(me);
		 return super.onTouchEvent(me); 		          
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		//Log.d(TextTV.TAG,"Fling detected. VelocityX = " + velocityX + "VelocityY = " + velocityY);
		if ((Parent != null) && (Math.abs(velocityY) < Math.abs(velocityX)))
		{				
			if (velocityX > 200)
			{
				//Fling scroll left 
				Parent.PrevPage();
			}
			else if (velocityX < -200)
			{
				//Fling scroll right
				Parent.NextPage();				
			}
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
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
	
	public void SetParent(TextTV Parent)
	{
		this.Parent = Parent;
	}
}
