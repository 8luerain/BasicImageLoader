package xiaomeng.bupt.com.imageloader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.widget.GridView;

/**
 * Project: ImageLoader.
 * Data: 2016/7/26.
 * Created by 8luerain.
 * Contact:<a href="mailto:8luerain@gmail.com">Contact_me_now</a>
 */
public class VelocityTrackGridview extends GridView {
    private static final String TAG = "VelocityTrackGridview";

    private VelocityTracker mVelocityTracker;
    private float mYVelocity;

    public VelocityTrackGridview(Context context) {
        this(context, null);
    }

    public VelocityTrackGridview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VelocityTrackGridview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mVelocityTracker = VelocityTracker.obtain();
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mVelocityTracker.addMovement(ev);
        mVelocityTracker.computeCurrentVelocity(100);
        mYVelocity = mVelocityTracker.getYVelocity();
        float yVelocity = mYVelocity;
        Log.d(TAG, "onTouchEvent: yVelocity [" + yVelocity + "]");
        return super.onTouchEvent(ev);
    }

    public float getYVelocity() {
        return mYVelocity;
    }

}
