package com.stickercamera.app.camera.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.util.DistanceUtil;
import com.github.skykai.stickercamera.R;
import com.stickercamera.app.camera.effect.FilterEffect;
import com.stickercamera.app.camera.util.GPUImageFilterTools;

import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * @author tongqian.ni
 */
public class FilterAdapter extends BaseAdapter {

    List<FilterEffect> filterUris;
    Context mContext;
    private Bitmap background;

    private int selectFilter = 0;

    public void setSelectFilter(int selectFilter) {
        this.selectFilter = selectFilter;
    }

    public int getSelectFilter() {
        return selectFilter;
    }

    public FilterAdapter(Context context, List<FilterEffect> effects, Bitmap backgroud) {
        filterUris = effects;
        mContext = context;
        this.background = backgroud;
    }

    public void setBackground(Bitmap backgroud) {
        this.background = backgroud;
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
            convertView = layoutInflater.inflate(R.layout.item_bottom_filter, null);
            holder = new EffectHolder();
            holder.filteredImg = (GPUImageView) convertView.findViewById(R.id.small_filter);
            holder.filterName = (TextView) convertView.findViewById(R.id.filter_name);
            holder.iv_bg = (ImageView) convertView.findViewById(R.id.iv_bg);
            convertView.setTag(holder);
        } else {
            holder = (EffectHolder) convertView.getTag();
        }
        ViewGroup.LayoutParams params = holder.filteredImg.getLayoutParams();
        params.width = DistanceUtil.getScreenW((Activity) mContext) / 5;
        params.height = DistanceUtil.getScreenW((Activity) mContext) / 5;
        holder.filteredImg.setLayoutParams(params);

        ViewGroup.LayoutParams params2 = holder.iv_bg.getLayoutParams();
        params2.width = DistanceUtil.getScreenW((Activity) mContext) / 5;
        params2.height = DistanceUtil.getScreenW((Activity) mContext) / 5;
        holder.iv_bg.setLayoutParams(params2);

        ViewGroup.LayoutParams params1 = holder.filterName.getLayoutParams();
        params1.width = DistanceUtil.getScreenW((Activity) mContext) / 5;
        holder.filterName.setLayoutParams(params1);

        if (position == getSelectFilter()) {
            holder.iv_bg.setVisibility(View.VISIBLE);
            holder.filterName.setTextColor(Color.rgb(255,0,0));
        } else {
            holder.iv_bg.setVisibility(View.GONE);
            holder.filterName.setTextColor(Color.rgb(0,0,0));
        }

        final FilterEffect effect = (FilterEffect) getItem(position);

        holder.filteredImg.setImage(background);
        holder.filterName.setText(effect.getTitle());
        //if (!effect.isOri() && effect.getType() != null) {
        GPUImageFilter filter = GPUImageFilterTools.createFilterForType(mContext, effect.getType());
        holder.filteredImg.setFilter(filter);

        return convertView;
    }

    class EffectHolder {
        GPUImageView filteredImg;
        ImageView iv_bg;
        TextView filterName;
    }

}
