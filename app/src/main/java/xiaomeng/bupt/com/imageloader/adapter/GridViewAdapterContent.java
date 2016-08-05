package xiaomeng.bupt.com.imageloader.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import xiaomeng.bupt.com.imageloader.R;
import xiaomeng.bupt.com.imageloader.utils.ImageLoader;
import xiaomeng.bupt.com.imageloader.view.SquerImageView;
import xiaomeng.bupt.com.imageloader.view.VelocityTrackGridview;

/**
 * Created by rain on 2016/7/23.
 */
public class GridViewAdapterContent extends BaseAdapter {
    private static final String TAG = "GridViewAdapterContent";

    private Context mContext;

    private LayoutInflater mLayoutInflater;

    private List<String> mUrls;

    private int mResouceId;
    private ImageLoader mInstance;

    private VelocityTrackGridview mVelocityTrackGridview;

    private boolean mIsScollIdel = true;


    public GridViewAdapterContent(Context mContext, int resouceId) {
        this.mContext = mContext;
        mResouceId = resouceId;
        mLayoutInflater = LayoutInflater.from(mContext);
        mInstance = ImageLoader.build(mContext);
    }

    public void setVelocityTrackGridview(VelocityTrackGridview velocityTrackGridview) {
        mVelocityTrackGridview = velocityTrackGridview;
    }

    public List<String> getmUrls() {
        return mUrls;
    }

    public void setmUrls(List<String> mUrls) {
        this.mUrls = mUrls;
    }

    @Override
    public int getCount() {
        return mUrls.size();
    }

    @Override
    public String getItem(int position) {
        return mUrls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(mResouceId, null);
            ViewHolder holder = new ViewHolder();
            holder.squerImageView = (SquerImageView) convertView.findViewById(R.id.squerview_item_grid_view);
            convertView.setTag(holder);
        }

        String url = getItem(position);
        ImageView imageView = ((ViewHolder) convertView.getTag()).squerImageView;
        if (!TextUtils.equals(url, (String) imageView.getTag())) {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }

        if (Math.abs(mVelocityTrackGridview.getYVelocity()) <= 150f) {
            mInstance.bindBitmap(imageView, url,
                    imageView.getMeasuredWidth(), imageView.getMeasuredHeight());
            imageView.setTag(url);
        }
        return convertView;
    }

    public void setScollIdel(boolean scollIdel) {
        mIsScollIdel = scollIdel;
    }

    class ViewHolder {
        public SquerImageView squerImageView;
    }
}
