package xiaomeng.bupt.com.imageloader.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by rain on 2016/7/23.
 */
public class SquerImageView extends ImageView {


    public SquerImageView(Context context) {
        this(context, null);
    }

    public SquerImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
