package com.stickercamera.app.camera.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.common.util.DistanceUtil;
import com.common.util.ImageUtils;
import com.github.skykai.stickercamera.R;
import com.stickercamera.photopick.ImageInfo;
import com.stickercamera.photopick.XImageView;

import java.util.List;


/**
 * 标题栏小图图片
 */
public class GetImgAdapter extends BaseAdapter {

    private Context mContext;
    private List<ImageInfo> values;
    private int clickPosition;//当前点击图片

    public int getClickPosition() {
        return clickPosition;
    }

    public void setClickPosition(int clickPosition) {
        this.clickPosition = clickPosition;
    }

    public GetImgAdapter(Context context, List<ImageInfo> values) {
        this.mContext = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        int size = values.size();
        if (size < 9) {
            size++;
        }
        return size;
    }

    @Override
    public Object getItem(int position) {
        return values.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        HolderView holder;
        if (convertView == null) {
            holder = new HolderView();
            convertView = View.inflate(mContext, R.layout.adapter_getimg, null);
            holder.mIvDeleteimg = (ImageView) convertView.findViewById(R.id.iv_deleteimg);
            holder.mIvGetimg = (XImageView) convertView.findViewById(R.id.iv_getimg);
            convertView.setTag(holder);
        } else {
            holder = (HolderView) convertView.getTag();
        }
        ViewGroup.LayoutParams params = holder.mIvGetimg.getLayoutParams();
        params.width = DistanceUtil.getScreenW((Activity) mContext) / 9;
        params.height = DistanceUtil.getScreenW((Activity) mContext) / 9;
        holder.mIvGetimg.setLayoutParams(params);

        ViewGroup.LayoutParams params1 = holder.mIvDeleteimg.getLayoutParams();
        params1.width = DistanceUtil.getScreenW((Activity) mContext) / 9;
        params1.height = DistanceUtil.getScreenW((Activity) mContext) / 9;
        holder.mIvDeleteimg.setLayoutParams(params1);

        if (position == getClickPosition()) {
            holder.mIvDeleteimg.setVisibility(View.VISIBLE);
        } else {
            holder.mIvDeleteimg.setVisibility(View.GONE);
        }
        try {
            if (values.size() == 1 && position == 1) {
                holder.mIvGetimg.setImageResource(R.drawable.image_add_default);
            } else {
                ImageInfo item = values.get(position);
                ImageUtils.asyncLoadSmallImage(mContext, Uri.parse(item.path), new ImageUtils.LoadImageCallback() {
                    @Override
                    public void callback(Bitmap result) {
                        if (result != null) {
                            holder.mIvGetimg.setImageBitmap(result);
                        }

                    }
                });
            }
        } catch (Exception e) {
            holder.mIvGetimg.setImageResource(R.drawable.image_add_default);
            holder.mIvDeleteimg.setVisibility(View.GONE);
        }
//        convertView.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                setClickPosition(position);
//                notifyDataSetChanged();
//            }
//        });
        return convertView;
    }

    public static class HolderView {
        XImageView mIvGetimg;
        ImageView mIvDeleteimg;
    }

}
