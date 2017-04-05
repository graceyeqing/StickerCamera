package com.stickercamera.photopick;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.github.skykai.stickercamera.R;


/**
 * Created by tian on 15-9-10.
 */

public class AllPhotoAdapter extends GridPhotoAdapter {

    private Context mContext;

    public AllPhotoAdapter(Context context, Cursor c, boolean autoRequery, PhotoPickActivity activity) {
        super(context, c, autoRequery, activity);
        mContext = context;
    }

    @Override
    public int getCount() {
        return super.getCount() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (position > 0) {
            return super.getItem(position - 1);
        } else {
            return super.getItem(position);
        }
    }

    @Override
    public long getItemId(int position) {
        if (position > 0) {
            return super.getItemId(position - 1);
        } else {
            return -1;
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position > 0) {
            return super.getDropDownView(position - 1, convertView, parent);
        } else {
            return getView(position, convertView, parent);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position > 0) {
            return super.getView(position - 1, convertView, parent);
        } else {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_photopick_gridlist_camera, parent, false);

                convertView.getLayoutParams().height = (int) (mContext.getResources().getDisplayMetrics().widthPixels/3 - dimension);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mActivity.camera();
                    }
                });
            }

            return convertView;
        }
    }
}

