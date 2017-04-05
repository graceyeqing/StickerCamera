package com.stickercamera.app.camera.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.skykai.stickercamera.R;

import java.util.List;

/**
 * 
 * 贴纸类型适配器
 * @author tongqian.ni
 */
public class StickerTagAdapter extends BaseAdapter {

    List<String> filterUris;
    Context     mContext;
    private int currentTag;
    public StickerTagAdapter(Context context, List<String> effects) {
        filterUris = effects;
        mContext = context;
    }
    public void setCurrentTag(int index){
        this.currentTag=index;
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
            convertView = layoutInflater.inflate(R.layout.item_bottom_tag, null);
            holder = new EffectHolder();
            holder.tag = (TextView) convertView.findViewById(R.id.effect_tag);
            convertView.setTag(holder);
        } else {
            holder = (EffectHolder) convertView.getTag();
        }
        if(position==currentTag){
            holder.tag.setTextColor(Color.rgb(255,0,0));
            holder.tag.setTextSize(18);
        }else{
            holder.tag.setTextColor(Color.rgb(122,110,110));
            holder.tag.setTextSize(13);
        }
        final String effect = (String) getItem(position);
        holder.tag.setText(effect);
        return convertView;
    }

    class EffectHolder {
        TextView tag;
    }

}
