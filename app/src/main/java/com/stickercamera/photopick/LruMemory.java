package com.stickercamera.photopick;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * Created by tian on 15-10-8.
 */
public class LruMemory extends LruCache<String,Bitmap> {
    /**
     */
    private LruMemory() {
        super((int) (Runtime.getRuntime().maxMemory() / 8));
    }
    private static LruMemory lruMemory;
    public static LruMemory getInstance(){
        synchronized (LruMemory.class){
            if(lruMemory == null){
                synchronized (LruMemory.class){
                    lruMemory = new LruMemory();
                }
            }
        }
        return lruMemory;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() ;
    }

    public Bitmap getBitmap(String key){
        return super.get(key);
    }

    public void addBitmap(String key,Bitmap bitmap){
        if(getBitmap(key) == null){
            super.put(key,bitmap);
        }
    }

    /**
     *退出ａｃｔｉｖｉｔｙ时记得清除内存
     */
    public void clearMomery(){
        super.trimToSize(-1);
    }
}
