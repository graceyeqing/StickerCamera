package com.stickercamera.photopick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.skykai.stickercamera.R;

import java.util.ArrayList;

/**
 * Created by tian on 15-9-9.
 */

public class FolderAdapter extends BaseAdapter {

    private String mSelect = "";
    ArrayList<ImageInfoExtra> mFolderData = new ArrayList<>();

    public FolderAdapter(ArrayList<ImageInfoExtra> mFolderData,Context context) {
        this.mFolderData = mFolderData;
    }

    public String getSelect() {
        return mSelect;
    }

    public void setSelect(int pos) {
        if (pos >= getCount()) {
            return;
        }

        mSelect = mFolderData.get(pos).getmName();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolderData.size();
    }

    @Override
    public Object getItem(int position) {
        return mFolderData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_photopick_list, parent, false);
            holder = new ViewHolder();
            holder.foldIcon = (ImageView) convertView.findViewById(R.id.foldIcon);
            holder.foldName = (TextView) convertView.findViewById(R.id.foldName);
            holder.photoCount = (TextView) convertView.findViewById(R.id.photoCount);
            holder.check = convertView.findViewById(R.id.check);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ImageInfoExtra data = mFolderData.get(position);
        String uri = data.getPath();
        int count = data.getCount();

        holder.foldName.setText(data.getmName());
        holder.photoCount.setText(String.format("%d张", count));

        String path = ImageInfo.pathAddPreFix(uri);
        String temPath = path.substring(path.indexOf("file://") + 7);
        holder.foldIcon.setTag(temPath);
        ImageLoad.getInstance().load(temPath,holder.foldIcon,holder.foldIcon.getLayoutParams().width,holder.foldIcon.getLayoutParams().height);

        if (data.getmName().equals(mSelect)) {
            holder.check.setVisibility(View.VISIBLE);
        } else {
            holder.check.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView foldIcon;
        TextView foldName;
        TextView photoCount;
        View check;
    }
}
