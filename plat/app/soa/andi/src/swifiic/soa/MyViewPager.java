package swifiic.soa;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class MyViewPager extends ViewPager {

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean blockSwipe = false;
    public void setBlockSwipe(boolean blockSwipe) {
        this.blockSwipe = blockSwipe;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
    		//return !blockSwipe;
    	if (!blockSwipe)	
    		return super.onInterceptTouchEvent(arg0);
    	else return false;
    }

}
