package com.stickercamera.photopick;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.github.skykai.stickercamera.R;


/**
 * Created by tian on 15-9-10.
 */
public class ImageFragment extends Fragment {

//    public static DisplayImageOptions optionsImage = new DisplayImageOptions
//            .Builder()
//            .showImageForEmptyUri(R.mipmap.ic_launcher)
//            .showImageOnFail(R.mipmap.ic_launcher)
//            .bitmapConfig(Bitmap.Config.RGB_565)
//            .cacheOnDisk(true)
//            .resetViewBeforeLoading(true)
//            .cacheInMemory(false)
//            .considerExifParams(true)
//            .imageScaleType(ImageScaleType.EXACTLY)
//            .build();
    ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_image_pager_item,container,false);
        imageView = (ImageView) view.findViewById(R.id.imageLoad);

        Bundle bundle = getArguments();
        final String path = bundle.getString("url");
//        ImageLoader.getInstance().displayImage(path,imageView,optionsImage);
        final String temPath = path.substring(path.indexOf("file://") + 7);
        imageView.setTag(temPath);
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                ImageLoad.getInstance().load(temPath, imageView, imageView.getMeasuredWidth(), imageView.getMeasuredHeight());

                Log.d("unlock", imageView.getMeasuredWidth() + "--" + imageView.getMeasuredHeight());
                imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        return view;
    }

}
