package xiaomeng.bupt.com.imageloader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by rain on 2016/7/23.
 */
public class ImageLoader {

    private static final String TAG = "ImageLoader";

    public static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    public static final int EXECUTE_CORE_SIZE = CPU_COUNT + 1;
    public static final int EXECUTE_MAX_SIZE = CPU_COUNT * 2 + 1;
    public static final long MAX_LIVE_TIME = 10l;
    public static final int DISK_CACHE_SIZE = 1024 * 1024 * 50; //50MB

    private static final int BUFFERED_SIZE = 1024 * 4;

    private static final int MESSAGE_HANDLER_RESULE = 1000;

    private static boolean isDiskCacheCreate;

    private static Handler sHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_HANDLER_RESULE:
                    ImageLoaderResult result = (ImageLoaderResult) msg.obj;
                    ImageView imageView = result.imageView;
                    imageView.setImageBitmap(result.bitmap);
                    break;
            }
        }
    };

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        AtomicInteger mThreadCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ImageLoader#" + mThreadCount.getAndIncrement());
        }
    };

    private static final ExecutorService THREAD_POOL_EXECUTOR_SERVICE = new ThreadPoolExecutor(
            EXECUTE_CORE_SIZE,
            EXECUTE_MAX_SIZE,
            MAX_LIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),
            THREAD_FACTORY);


    private Context mContext;

    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;
    private boolean isPause;

    private ImageLoader(Context context) {
        mContext = context;
        mLruCache = new LruCache<String, Bitmap>(getLRUCacheSize()) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024; //KB
            }
        };
        try {
            mDiskLruCache = DiskLruCache.open(
                    getDiskCacheDir(mContext, "bitmap"),
                    1, 1, DISK_CACHE_SIZE);
            isDiskCacheCreate = true;
        } catch (IOException e) {
            e.printStackTrace();
            isDiskCacheCreate = false;
        }

    }

    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }


    public Bitmap loadBitmap(String url, int requestWidth, int requestHeight) {

        if (isMainThread()) {
            throw new RuntimeException("run in the mainThread , it not recommend");
        }

        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("has no url");
        }
        return getBitmap(url, requestWidth, requestHeight);
    }

    public void bindBitmap(final ImageView imageView, final String url, final int requstWidth, final int requestHeight) {

        Runnable downloadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Message message = sHandler.obtainMessage();
                message.what = MESSAGE_HANDLER_RESULE;
                message.obj = new ImageLoaderResult(imageView, getBitmap(url, requstWidth, requestHeight));
                message.sendToTarget();
            }
        };

        THREAD_POOL_EXECUTOR_SERVICE.execute(downloadBitmapTask);

    }

    public void pause() {
        isPause = true;
    }

    public void start() {
        isPause = false;
    }


    public Bitmap getBitmap(String url, int requestWidth, int requestHeight) {
        Bitmap bitmap;

        String key = hashKeyForUrl(url);

        bitmap = getbitmapFromMemeryCache(url);

        if (bitmap != null) {
            Log.d(TAG, "getBitmap: form memory....");
            return bitmap;
        }

        bitmap = getBitmapFromDiskCache(key);
        if (bitmap != null) {
            Log.d(TAG, "getBitmap: form deskCache....");
            return bitmap;
        }

        bitmap = loadBitmapFromHttpAndPutDiskCache(url, requestWidth, requestHeight);

        if (null != bitmap)
            mLruCache.put(key, bitmap);

        return bitmap;
    }

    private Bitmap getbitmapFromMemeryCache(String url) {
        return mLruCache.get(hashKeyForUrl(url));
    }

    private Bitmap getBitmapFromDiskCache(String url) {
        String key = hashKeyForUrl(url);

        if (!isDiskCacheCreate) {
            return loadBitmapFromHttpWithoutDiskCache(url);
        }

        try {
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                return BitmapFactory.decodeStream(snapshot.getInputStream(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getLRUCacheSize() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 8);
        return maxMemory / 8;
    }


    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private Bitmap loadBitmapFromHttpWithoutDiskCache(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return BitmapFactory.decodeStream(
                        new BufferedInputStream(connection.getInputStream(), BUFFERED_SIZE));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap loadBitmapFromHttpAndPutDiskCache(String url, int requestWidth, int requestHeight) {

        Log.d(TAG, "loadBitmapFromHttpAndPutDiskCache: download image. ......");
        String key = hashKeyForUrl(url);

        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (null != editor) {
                OutputStream outputStream = editor.newOutputStream(0);
                if (downloadFile(url, outputStream)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
//                return BitmapAnalysiser.getRequestSizeBitMap(requestWidth, requestHeight,
//                            ((FileInputStream) mDiskLruCache.get(key).getInputStream(0)).getFD());
                mDiskLruCache.flush();
                DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                if (null != snapshot) {
                    return BitmapFactory.decodeStream(snapshot.getInputStream(0));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private boolean downloadFile(String url, OutputStream outputStream) {
        HttpURLConnection connection;
        BufferedInputStream BIS = null;
        BufferedOutputStream BOS = null;
        byte[] buffer = new byte[BUFFERED_SIZE];
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
                return false;
            BIS = new BufferedInputStream(connection.getInputStream());
            BOS = new BufferedOutputStream(outputStream);
            for (int count = 0; count != -1; count = BIS.read(buffer))
                BOS.write(buffer, 0, count);
            BOS.flush();

        } catch (Exception e) {
            e.printStackTrace();
            return false;

        } finally {
            closeQuilty(BIS);
            closeQuilty(BOS);
        }
        return true;
    }

    public static void closeQuilty(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getDiskCacheDir(Context context, String uniqueName) {

        String filePath;

        boolean isMounted = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);

        if (isMounted) {
            filePath = context.getExternalCacheDir().getAbsolutePath();
        } else {
            filePath = context.getCacheDir().getAbsolutePath();
        }
        File cacheDirFile = new File(filePath + File.separator + uniqueName);
        if (!cacheDirFile.exists()) {
            cacheDirFile.mkdir();
        }
        Log.d(TAG, "getDiskCacheDir: cacheDirPath [" + cacheDirFile.getAbsolutePath() + "]");

        return cacheDirFile;
    }

    private long getUsableSpace(File file) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD) {
            return file.getUsableSpace();
        } else {
            StatFs statFs = new StatFs(file.getPath());
            return statFs.getAvailableBytes();
        }
    }

    private String hashKeyForUrl(String url) {
        String hashKey;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            hashKey = byteToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            hashKey = String.valueOf(url.hashCode());
        }

        return hashKey;
    }

    private String byteToString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String ss = Integer.toHexString(0xFF & bytes[i]);
            if (ss.length() == 1) builder.append("0");
            builder.append(ss);
        }
        return builder.toString();
    }


    static class BitmapAnalysiser {

        public static Bitmap getRequestSizeBitMap(int requestWidth, int requestHeight, File file) {
            Bitmap bitmap = null;
            FileInputStream FIS = null;
            try {
                FIS = new FileInputStream(file);
                getRequestSizeBitMap(requestWidth, requestHeight, FIS.getFD());

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                ImageLoader.closeQuilty(FIS);
            }
            return bitmap;
        }

        public static Bitmap getRequestSizeBitMap(int requestWidth, int requestHeight, FileDescriptor fileDescriptor) {
            Bitmap bitmap;
            int inSampleSize = 1;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            int bitmapWidth = bitmap.getWidth();
            int bitmapHeight = bitmap.getHeight();
            if (requestWidth > bitmapWidth || requestHeight > bitmapHeight) {
                while (bitmapWidth <= requestWidth && bitmapHeight <= requestHeight)
                    inSampleSize *= 2;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSampleSize;
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            return bitmap;
        }

    }

    class ImageLoaderResult {

        public ImageView imageView;

        public Bitmap bitmap;

        public ImageLoaderResult(ImageView imageView, Bitmap bitmap) {
            this.imageView = imageView;
            this.bitmap = bitmap;
        }
    }
}
