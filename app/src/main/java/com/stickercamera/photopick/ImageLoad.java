package com.stickercamera.photopick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.common.util.ImageUtils;
import com.github.skykai.stickercamera.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tian on 15-10-8.
 */
public class ImageLoad {

    ExecutorService threadPool;
    private ImageLoad() {
        threadPool = Executors.newFixedThreadPool(4);
    }
    private static ImageLoad imageLoad;
    public static ImageLoad getInstance(){
        synchronized (ImageLoad.class){
            if(imageLoad == null){
                synchronized (ImageLoad.class){
                    imageLoad = new ImageLoad();
                }
            }
        }
        return imageLoad;
    }

    private String generateLruKey(String imageurl,int width,int height){
        return new StringBuilder(imageurl).append("_").append(width).append("_").append(height).toString();
    }


    public void load(String path,ImageView imageView,int width,int height){
        if(path.contains("file://")){
            path = path.substring(path.indexOf("file://") + 7);
        }
        imageView.setTag(path);
        String generateLruKey = generateLruKey(path, width, height);
        Bitmap bitmap = LruMemory.getInstance().getBitmap(generateLruKey);
        if(bitmap !=null ){
            if(imageView.getTag().equals(path)){
                imageView.setImageBitmap(bitmap);
            }
        }else{

            imageView.setImageResource(R.drawable.default_image);
            threadPool.execute(new BitmapLoadRunnable(width,height,imageView,path));
        }

    }

    Handler handler = new Handler(Looper.getMainLooper());

    class BitmapLoadRunnable implements Runnable{

        private String imageUrl;
        private int width;
        private int height;
        private ImageView imageView;

        public BitmapLoadRunnable(int width,int height, ImageView imageView, String url) {
            this.width = width;
            this.height = height;
            this.imageView = imageView;
            this.imageUrl = url;
        }

        @Override
        public void run() {
            Bitmap bitmap;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageUrl,options);
            int actualWidth = options.outWidth;
            int actualHeight = options.outHeight;
            options.inJustDecodeBounds = false;
            options.inSampleSize = calImageScaleSize(actualWidth, actualHeight, width, height);

            bitmap = BitmapFactory.decodeFile(imageUrl, options);
            /** 获取图片的旋转角度，有些系统把拍照的图片旋转了，有的没有旋转
                    */
            int degree = ImageUtils.getImageDegrees(imageUrl);
            /**
             * 把图片旋转为正的方向
             */
            Bitmap newbitmap = ImageUtils.imageWithFixedRotation(bitmap,degree);
            if(bitmap != null)
                LruMemory.getInstance().addBitmap(generateLruKey(imageUrl,width,height), newbitmap);

            handler.post(new DisplayTask(new ImageInfo(imageUrl,newbitmap,imageView)));
        }
    }
    class DisplayTask implements Runnable{

        private ImageInfo imageInfo;

        public DisplayTask(ImageInfo imageInfo) {
            this.imageInfo = imageInfo;
        }

        @Override
        public void run() {
            if(imageInfo.imageView.getTag().equals(imageInfo.path))
                imageInfo.imageView.setImageBitmap(imageInfo.bitmap);
        }
    }



    private int calImageScaleSize(int srcWidth,int srcHeight,int targetWidth,int targetHeight){
        float scale = 1.0f;
        float height = (float)srcHeight/(float)targetHeight;
        float width = (float)srcWidth/(float)targetWidth;
        float min = Math.min(height,width);
        while(scale<min){
            scale = scale * 2;
        }
        if(scale<1)
            scale = 1;
        return (int)scale;
    }

    class ImageInfo{
        String path;
        Bitmap bitmap;
        ImageView imageView;

        public ImageInfo(String path, Bitmap bitmap,ImageView imageView) {
            this.bitmap = bitmap;
            this.path = path;
            this.imageView = imageView;
        }
    }
}
