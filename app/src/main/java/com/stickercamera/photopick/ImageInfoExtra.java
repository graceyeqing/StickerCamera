package com.stickercamera.photopick;

/**
 * 文件夹bean，包含文件夹名称以及第一张图片信息
 * Created by tian on 15-9-9.
 */
public class ImageInfoExtra {

    private ImageInfo mImageInfo;
    private int mCount = 0;
    private String mName = "";

    public ImageInfoExtra(String name, ImageInfo mImageInfo, int count) {
        mName = name;
        this.mImageInfo = mImageInfo;
        mCount = count;
    }

    public String getPath() {
        return mImageInfo.path;
    }

    public int getCount() {
        return mCount;
    }

    public String getmName() {
        return mName;
    }

    @Override
    public String toString() {
        return "ImageInfoExtra{" +
                "mImageInfo=" + mImageInfo +
                ", mCount=" + mCount +
                ", mName='" + mName + '\'' +
                '}';
    }
}
