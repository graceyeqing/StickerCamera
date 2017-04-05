package com.stickercamera.photopick;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.skykai.stickercamera.R;

import java.util.ArrayList;

import static com.stickercamera.photopick.PhotoPickActivity.deleteNum;


/**
 * Created by tian on 15-9-10.
 */
public class PhotoPickDetailActivity extends AppCompatActivity {


    public static final String PICK_DATA = "PICK_DATA";
    public static final String ALL_DATA = "ALL_DATA";
    public static final String FOLDER_NAME = "FOLDER_NAME";
    public static final String PHOTO_BEGIN = "PHOTO_BEGIN";
    public static final String EXTRA_MAX = "EXTRA_MAX";

    public static final String RESULT_DATA = "data";
    public static final String RESULT_SEND = "send";



    private ArrayList<ImageInfo> mPickPhotos;
    private ArrayList<ImageInfo> mAllPhotos;

    private int mMaxPick = 6;
    private MenuItem mMenuSend;
    private final String actionbarTitle = "%d/%d";
    Cursor mCursor;

    private ViewPager mViewPager;
    private CheckBox mCheckBox;

    private TextView tvRight;
    private ImageView tvLeft;
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick_detail);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvRight= (TextView) findViewById(R.id.include_title).findViewById(R.id.base_title_tv_right);
        tvLeft= (ImageView) findViewById(R.id.include_title).findViewById(R.id.base_title_tv_left);
        Bundle extras = getIntent().getExtras();
        mPickPhotos = (ArrayList<ImageInfo>) extras.getSerializable(PICK_DATA);
        mAllPhotos = (ArrayList<ImageInfo>) extras.getSerializable(ALL_DATA);

        int mBegin = extras.getInt(PHOTO_BEGIN, 0);
        mMaxPick = extras.getInt(EXTRA_MAX, 5);
        if(mAllPhotos == null){
            String folderName = extras.getString(FOLDER_NAME, "");
            String where = folderName;
            if (!folderName.isEmpty()) {
                where = String.format("%s='%s'",
                        MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                        folderName);
            }
            mCursor = getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Images.ImageColumns._ID,
                            MediaStore.Images.ImageColumns.DATA},
                    where,
                    null,
                    MediaStore.MediaColumns.DATE_ADDED + " DESC");
        }

        ImagesAdapter adapter = new ImagesAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(mBegin);

        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                updateDisplay(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mViewPager.getCurrentItem();

                String uri = getImagePath(pos);

                if (((CheckBox) v).isChecked()) {
                    if (mPickPhotos.size() >= mMaxPick) {
                        ((CheckBox) v).setChecked(false);
                        String s = String.format("最多只能选%d张", mMaxPick);
                        Toast.makeText(PhotoPickDetailActivity.this, s, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    addPicked(uri);
                } else {
                    removePicked(uri);
                }

                updateDataPickCount();
            }
        });
        updateDisplay(mBegin);

        updateDataPickCount();

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAndSend(false);
            }
        });
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAndSend(false);
            }
        });
    }

    private void updateDataPickCount() {
        String send = String.format("确定(%d/%d)", mPickPhotos.size(), mMaxPick);
        tvRight.setText(send);
//        mMenuSend.setTitle(send);
    }

    private void addPicked(String path) {
        if (!isPicked(path)) {
            ImageInfo info = new ImageInfo(path);
            info.setCheckedNum(mPickPhotos.size() + 1);
            mPickPhotos.add(info);
//            mPickPhotos.add(new ImageInfo(path));
        }
    }

    private void removePicked(String path) {
        int deleteIndex = -1;
        for (int i = 0; i < mPickPhotos.size(); ++i) {
            if (mPickPhotos.get(i).path.equals(path)) {
                deleteNum = mPickPhotos.get(i).checkedNum;
                mPickPhotos.get(i).checkedNum = 0;
                deleteIndex = i;
            } else {
                //设置显示数字
                if (mPickPhotos.get(i).getCheckedNum() > deleteNum && deleteNum > 0) {
                    mPickPhotos.get(i).setCheckedNum(mPickPhotos.get(i).getCheckedNum() - 1);
                }
            }
        }
        if (deleteIndex > -1) {
            mPickPhotos.remove(deleteIndex);
        }

//        for (int i = 0; i < mPickPhotos.size(); ++i) {
//            if (mPickPhotos.get(i).path.equals(path)) {
//                mPickPhotos.remove(i);
//                return;
//            }
//        }
    }
    private void updateDisplay(int pos) {
        String uri = getImagePath(pos);
        mCheckBox.setChecked(isPicked(uri));
        tvRight.setText(String.format(actionbarTitle, pos + 1, getImageCount()));
//        getSupportActionBar().setTitle(String.format(actionbarTitle, pos + 1, getImageCount()));
    }

    private boolean isPicked(String path) {
        for (ImageInfo item : mPickPhotos) {
            if (item.path.equals(path)) {
                return true;
            }
        }

        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_photo_pick,menu);
//        mMenuSend = menu.getItem(0);
//        updateDataPickCount();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            selectAndSend(false);
            return true;
        }else if(id == R.id.action_finish){
            selectAndSend(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            selectAndSend(false);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void selectAndSend(boolean send) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_DATA, mPickPhotos);
        intent.putExtra(RESULT_SEND, send);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    class ImagesAdapter extends FragmentStatePagerAdapter {

        public ImagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            ImageFragment imageFragment = new ImageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url",getImagePath(position));
            imageFragment.setArguments(bundle);

            return imageFragment;
        }

        @Override
        public int getCount() {
            return getImageCount();
        }
    }


    String getImagePath(int pos) {
        if (mAllPhotos != null) {
            return mAllPhotos.get(pos).path;
        } else {
            String path = "";
            if (mCursor.moveToPosition(pos)) {
                path = ImageInfo.pathAddPreFix(mCursor.getString(1));
            }
            return path;
        }
    }

    private int getImageCount() {
        if(mAllPhotos != null){
            return mAllPhotos.size();
        }else{
            return mCursor.getCount();
        }
    }
}
