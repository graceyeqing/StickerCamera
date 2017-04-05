package com.stickercamera.app.camera.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.common.util.DistanceUtil;
import com.common.util.ImageLoaderUtils;
import com.github.skykai.stickercamera.R;
import com.stickercamera.app.model.Addon;

import java.util.List;

/**
 * 
 * 贴纸适配器
 * @author tongqian.ni
 */
public class StickerToolAdapter extends BaseAdapter {

    List<Addon> filterUris;
    Context     mContext;

    public StickerToolAdapter(Context context, List<Addon> effects) {
        filterUris = effects;
        mContext = context;
    }

    @Override
    public int getCount() {
        return filterUris.size();
    }

    @Override
    public Object getItem(int position) {
        return filterUris.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        EffectHolder holder = null;
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.item_bottom_tool, null);
            holder = new EffectHolder();
            holder.logo = (ImageView) convertView.findViewById(R.id.effect_image);
            holder.container = (ImageView) convertView.findViewById(R.id.effect_background);
            //holder.navImage.setOnClickListener(holder.clickListener);
            convertView.setTag(holder);
        } else {
            holder = (EffectHolder) convertView.getTag();
        }
        ViewGroup.LayoutParams params = holder.logo.getLayoutParams();
        params.width = DistanceUtil.getScreenW((Activity) mContext) / 4;
        params.height = DistanceUtil.getScreenW((Activity) mContext) / 4;
        holder.logo.setLayoutParams(params);

        ViewGroup.LayoutParams params1 = holder.container.getLayoutParams();
        params1.width = DistanceUtil.getScreenW((Activity) mContext) / 4;
        params1.height = DistanceUtil.getScreenW((Activity) mContext) / 4;
        holder.container.setLayoutParams(params1);
        final Addon effect = (Addon) getItem(position);

        return showItem(convertView, holder, effect);
    }

    private View showItem(View convertView, EffectHolder holder, final Addon sticker) {

        holder.container.setVisibility(View.GONE);
        ImageLoaderUtils.displayDrawableImage(sticker.getId() + "", holder.logo, null);

        return convertView;
    }

    class EffectHolder {
        ImageView logo;
        ImageView container;
    }

}
