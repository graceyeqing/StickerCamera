package com.stickercamera.photopick;

import android.content.Context;
import android.database.Cursor;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.skykai.stickercamera.R;


/**
 * Created by tian on 15-9-10.
 */

class GridPhotoAdapter extends CursorAdapter {

    final int itemWidth;
    LayoutInflater mInflater;
    PhotoPickActivity mActivity;
    View.OnClickListener mCheckItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mActivity.clickPhotoCheck(v);
        }
    };
    float dimension;

    GridPhotoAdapter(Context context, Cursor c, boolean autoRequery, PhotoPickActivity activity) {
        super(context, c, autoRequery);

        mInflater = LayoutInflater.from(context);
        mActivity = activity;
        dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
        itemWidth = (int) (context.getResources().getDisplayMetrics().widthPixels / 3 - dimension);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView = mInflater.inflate(R.layout.item_photopick_gridlist, parent, false);
        ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
        layoutParams.height = itemWidth;
        layoutParams.width = itemWidth;
        convertView.setLayoutParams(layoutParams);


        GridViewHolder holder = new GridViewHolder();
        holder.icon = (XImageView) convertView.findViewById(R.id.icon);

        holder.iconFore = (ImageView) convertView.findViewById(R.id.iconFore);
        holder.check = (CheckBox) convertView.findViewById(R.id.check);
        holder.layoutCheck = (FrameLayout) convertView.findViewById(R.id.layout_check);
        holder.checkNum = (TextView) convertView.findViewById(R.id.check_num);
        PhotoPickActivity.GridViewCheckTag checkTag = new PhotoPickActivity.GridViewCheckTag(holder.iconFore);
        checkTag.num = 0;
        holder.check.setTag(checkTag);
        holder.check.setOnClickListener(mCheckItem);

        holder.layoutCheck.setTag(checkTag);
        holder.layoutCheck.setOnClickListener(mCheckItem);
        convertView.setTag(holder);

        ViewGroup.LayoutParams iconParam = holder.icon.getLayoutParams();
        iconParam.width = itemWidth;
        iconParam.height = itemWidth;
        holder.icon.setLayoutParams(iconParam);

        return convertView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        GridViewHolder holder;
        holder = (GridViewHolder) view.getTag();

        final String path = ImageInfo.pathAddPreFix(cursor.getString(1));
        String temPath = path.substring(path.indexOf("file://") + 7);
        holder.icon.setTag(temPath);
        ImageLoad.getInstance().load(temPath, holder.icon, itemWidth, itemWidth);
        ((PhotoPickActivity.GridViewCheckTag) holder.check.getTag()).path = path;
        if (mActivity.getPhotoMode() == PhotoPickActivity.MODE_SINGLE_CROP) {
            holder.check.setVisibility(View.GONE);
            holder.layoutCheck.setVisibility(View.GONE);
            holder.iconFore.setVisibility(View.GONE);
            holder.icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.clickPhotoItem(path);
                }
            });
        } else {
            boolean isPick = mActivity.isPicked(path);
            holder.check.setChecked(isPick);
            holder.iconFore.setVisibility(holder.check.isChecked() ? View.VISIBLE : View.GONE);
            //显示数字
            holder.checkNum.setVisibility(holder.check.isChecked() ? View.VISIBLE : View.GONE);
            int checkedNum =mActivity.getPickedNum(path);
            int deleteNum = mActivity.getDeleteNum();
            if (holder.check.isChecked()&&checkedNum > 0) {
                if (deleteNum > 0 && checkedNum > deleteNum) {
                    holder.checkNum.setText(checkedNum + "");
                    ((PhotoPickActivity.GridViewCheckTag) holder.check.getTag()).num = checkedNum;
                } else {
                    holder.checkNum.setText(checkedNum + "");
                }
            }
        }

    }

    static class GridViewHolder {
        XImageView icon;
        ImageView iconFore;
        CheckBox check;
        FrameLayout layoutCheck;
        TextView checkNum;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
