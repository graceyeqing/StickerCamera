package com.stickercamera.photopick;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/**
 * Created by tian on 15-9-10.
 */
public class PhotoPickAnimation {

    public static Animation translateAnimation(float fromX,float toX,float fromY,float toY ,long durationMillis,
                     boolean fillAfter,Animation.AnimationListener listener){
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,fromX,Animation.RELATIVE_TO_PARENT,toX,Animation.RELATIVE_TO_PARENT,fromY,Animation.RELATIVE_TO_PARENT,toY);
        translateAnimation.setDuration(durationMillis);
        translateAnimation.setFillAfter(fillAfter);
        translateAnimation.setAnimationListener(listener);
        return translateAnimation;
    }

    public static Animation alphaAnimation(float fromAlpha,float toAlpha,long durationMillis,
            boolean fillAfter,Animation.AnimationListener listener){

        AlphaAnimation alphaAnimation = new AlphaAnimation(fromAlpha,toAlpha);
        alphaAnimation.setDuration(durationMillis);
        alphaAnimation.setFillAfter(fillAfter);
        alphaAnimation.setAnimationListener(listener);
        return alphaAnimation;
    }
}
