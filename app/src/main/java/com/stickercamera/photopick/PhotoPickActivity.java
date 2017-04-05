package com.stickercamera.photopick;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.skykai.stickercamera.R;
import com.stickercamera.base.BaseActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * 图片选择控件,有两种模式
 * <p>1.MODE_SINGLE_CROP,选择一张图片并且裁剪返回,适用于头像选择</p>
 * <h1>intent.putExtra(PhotoPickActivity.EXTRA_MODE, PhotoPickActivity.MODE_SINGLE_CROP);</h1>
 * <h1>startActivityForResult(intent, PhotoPickActivity.REQUEST_PHOTO_CROP);</h1>
 * <p>
 * <p>2.MODE_MUTIL_CROP,选择多张图片,并且返回一个list集合.</p>
 * <h1>intent.putExtra(PhotoPickActivity.EXTRA_MODE, PhotoPickActivity.MODE_MUTIL_CROP);</h1>
 * <h1>intent.putExtra(PhotoPickActivity.EXTRA_MAX, 6);//最大选择数</h1>
 * <h1>intent.putExtra(PhotoPickActivity.EXTRA_PICKED, imageInfos);当前已经选择的图片list</h1>
 * <p>
 * <p>在onActivityResult中获取数据</p>
 * <p>
 * <p>if(requestCode == PhotoPickActivity.REQUEST_PHOTO_CROP && resultCode == Activity.RESULT_OK)</p>
 * <p>bundle.getString(PhotoPickActivity.EXTRA_RESULT_CROP_PHOTO);//裁剪之后的图片路径</p>
 * <p>
 * <p>if(requestCode == PhotoPickActivity.REQUEST_PHOTO_LIST && resultCode == Activity.RESULT_OK)</p>
 * <p>ArrayList<ImageInfo> imageInfos = (ArrayList<ImageInfo>) data.getSerializableExtra(PhotoPickActivity.EXTRA_RESULT_PHOTO_LIST);//多个图片的路径集合</p>
 * <p>
 * <p>
 * Created by tian on 15-9-9.
 */
public class PhotoPickActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * 最大选择数
     */
    public static final String EXTRA_MAX = "EXTRA_MAX";//
    /**
     * 当前已经选择个数
     */
    public static final String EXTRA_PICKED = "EXTRA_PICKED"; //
    /**
     * 选择图片模式
     */
    public static final String EXTRA_MODE = "EXTRA_MODE";//
    /**
     * 图片裁剪模式
     */
    public static final int MODE_SINGLE_CROP = 11;
    /**
     * 多图片选择模式
     */
    public static final int MODE_MUTIL_CROP = 12;//
    /**
     * onactivityresult返回数据参数 （裁剪的图片）
     */
    public static final String EXTRA_RESULT_CROP_PHOTO = "cropPhoto"; //
    /**
     * onactivityresult返回数据参数（选择的图片list）
     */
    public static final String EXTRA_RESULT_PHOTO_LIST = "photoList"; //

    /**
     * resultCode图片裁剪
     */
    public static final int REQUEST_PHOTO_CROP = 13;
    /**
     * resultCode图片list
     */
    public static final int REQUEST_PHOTO_LIST = 14;
    public static final int REQUEST_STORAGE_PERMISSION = 1;

    private int mPhotoMode;//当前图片模式
    public static final int REQUEST_RESULT_PHOTO_CROP = 666; //
    public static final int REQUEST_RESULT_PHOTO = 665; //
    public static final int REQUEST_PHOTO_DETAIL = 664; //


    private static final int GRID_NUMCOLUMNS = 3;//列数
    private final String allPhotos = "所有图片";
//    private MenuItem mMenuItem;//actionBar按钮
    private TextView tvRight;//标题右侧按钮
    private ImageView tvLeft;//返回按钮
    private int mMaxPick;
    private TextView mPreView;
    private TextView mFoldName;
    private View mListViewGroup;
    private ListView mRecycleTitle;
    private FolderAdapter mFolderAdapter;
    private GridPhotoAdapter photoAdapter;
    private GridView mRecycleItem;
    private int mFolderId;
    private Uri fileCropUri;//裁剪的图片路径
    private Uri fileUri;//照相机照片路径
    public ArrayList<ImageInfo> mPickData = new ArrayList<>();


    //点击图片目录,显示.隐藏
    View.OnClickListener mOnClickFoldName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mListViewGroup.getVisibility() == View.VISIBLE) {
                hideFolderList();
            } else {
                showFolderList();
            }
        }
    };

    //点击预览,开启viewpager显示选中的图片
    View.OnClickListener onClickPre = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mPickData.size() == 0) {
                return;
            }
            Intent intent = new Intent(PhotoPickActivity.this, PhotoPickDetailActivity.class);
            intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData);
            intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick);
            intent.putExtra(PhotoPickDetailActivity.ALL_DATA, mPickData);
            String folderParam = "";
            intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, 0);
            folderParam = mFolderAdapter.getSelect();
            intent.putExtra(PhotoPickDetailActivity.FOLDER_NAME, folderParam);
            startActivityForResult(intent, REQUEST_PHOTO_DETAIL);
        }
    };


    private void showFolderList() {
        Animation translateAnimation = PhotoPickAnimation.translateAnimation(0, 0, 1.0f, 0f, 150, false, null);
        Animation alphaAnimation = PhotoPickAnimation.alphaAnimation(0f, 1f, 150, false, null);
        mListViewGroup.setAnimation(translateAnimation);
        mRecycleTitle.setAnimation(alphaAnimation);
        mListViewGroup.setVisibility(View.VISIBLE);
    }

    private void hideFolderList() {
        Animation translateAnimation = PhotoPickAnimation.translateAnimation(0, 0, 0.0f, 1.0f, 150, false, null);
        Animation alphaAnimation = PhotoPickAnimation.alphaAnimation(1f, 0f, 150, false, null);
        mListViewGroup.setAnimation(translateAnimation);
        mRecycleTitle.setAnimation(alphaAnimation);
        mListViewGroup.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_pick);
        initActionbar();
        init();
        initDirectory();
    }

    private void init() {
        mPhotoMode = getIntent().getIntExtra(EXTRA_MODE, MODE_SINGLE_CROP);
        mMaxPick = getIntent().getIntExtra(EXTRA_MAX, 1);
        Object extraPicked = getIntent().getSerializableExtra(EXTRA_PICKED);
        if (extraPicked != null) {
            mPickData = (ArrayList<ImageInfo>) extraPicked;
        }
        tvRight= (TextView) findViewById(R.id.include1).findViewById(R.id.base_title_tv_right);
        tvLeft= (ImageView) findViewById(R.id.include1).findViewById(R.id.base_title_tv_left);
        mRecycleTitle = (ListView) findViewById(R.id.rv_photo_title_list);
        mRecycleTitle.setOnItemClickListener(mOnItemClick);
//        mRecycleTitle.setLayoutManager(new WrapContentLinearLayoutManager(this));


        mRecycleItem = (GridView) findViewById(R.id.rv_photo_list);
        mRecycleItem.setOnItemClickListener(itemClickListener);
//        mRecycleItem.setLayoutManager(new GridLayoutManager(this,GRID_NUMCOLUMNS));

        mListViewGroup = findViewById(R.id.listViewParent);
        mListViewGroup.setOnClickListener(mOnClickFoldName);

        mFoldName = (TextView) findViewById(R.id.foldName);
        mFoldName.setText(allPhotos);

        findViewById(R.id.selectFold).setOnClickListener(mOnClickFoldName);

        mPreView = (TextView) findViewById(R.id.preView);
        mPreView.setOnClickListener(onClickPre);

        //完成按钮点击
        if (mPhotoMode == MODE_MUTIL_CROP) {
            updatePickCount();
        }

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT_PHOTO_LIST, mPickData);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    //初始化所有图片文件目录
    private void initDirectory() {

        int checkSelfPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//是否设置显示dialog
                new AlertDialog.Builder(PhotoPickActivity.this)
                        .setMessage("你需要启动权限WRITE_EXTERNAL_STORAGE")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Toast.makeText(PhotoPickActivity.this,"你需要启动权限WRITE_EXTERNAL_STORAGE",Toast.LENGTH_SHORT).show();
                                ActivityCompat.requestPermissions(PhotoPickActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            }
        } else {
            getCameraData();
        }
    }

    public void getCameraData() {

        final String[] needInfos = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
        };
        //存储每个类别及他里面图片的数量
        LinkedHashMap<String, Integer> mNames = new LinkedHashMap<>();
        //存储每个类别的第一张图片信息,在目录显示需要
        LinkedHashMap<String, ImageInfo> mData = new LinkedHashMap<>();
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, needInfos, "", null, MediaStore.MediaColumns.DATE_ADDED + " DESC");

        while (cursor.moveToNext()) {
            String name = cursor.getString(2);
            if (!mNames.containsKey(name)) {
                mNames.put(name, 1);//每个类别的数目
                ImageInfo imageInfo = new ImageInfo(cursor.getString(1));
                mData.put(name, imageInfo);//每个类别的第一张照片
            } else {
                int newCount = mNames.get(name) + 1;
                mNames.put(name, newCount);
            }
        }

        ArrayList<ImageInfoExtra> mFolderData = new ArrayList<>();//所有图片
        if (cursor.moveToFirst()) {//保存第一个项（所有图片的目录项）
            ImageInfo imageInfo = new ImageInfo(cursor.getString(1));
            int allImagesCount = cursor.getCount();
            mFolderData.add(new ImageInfoExtra(allPhotos, imageInfo, allImagesCount));
        }

        for (String item : mNames.keySet()) {//保存每个项目图片
            ImageInfo info = mData.get(item);
            Integer count = mNames.get(item);
            mFolderData.add(new ImageInfoExtra(item, info, count));
        }
        cursor.close();

        //显示目录列表信息
        mFolderAdapter = new FolderAdapter(mFolderData, this);
        mRecycleTitle.setAdapter(mFolderAdapter);

        getLoaderManager().initLoader(mFolderId, null, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCameraData();
                } else {
                    Toast.makeText(PhotoPickActivity.this, "获取权限失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private ListView.OnItemClickListener mOnItemClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mFolderAdapter.setSelect((int) id);
            String folderName = mFolderAdapter.getSelect();
            mFoldName.setText(folderName);
            hideFolderList();

            if (mFolderId != position) {
                getLoaderManager().destroyLoader(mFolderId);
                mFolderId = position;
            }
            getLoaderManager().initLoader(mFolderId, null, PhotoPickActivity.this);
        }
    };

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mPhotoMode == MODE_SINGLE_CROP) {
//                Uri uri = Uri.parse(mPickData.get(position).path);
//                fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
//                cropImageUri(uri, fileCropUri,640,640,REQUEST_RESULT_PHOTO_CROP);
            } else {
                Intent intent = new Intent(PhotoPickActivity.this, PhotoPickDetailActivity.class);
                intent.putExtra(PhotoPickDetailActivity.PICK_DATA, mPickData);
                intent.putExtra(PhotoPickDetailActivity.EXTRA_MAX, mMaxPick);
                String folderParam = "";
                intent.putExtra(PhotoPickDetailActivity.PHOTO_BEGIN, position - 1);
                folderParam = mFolderAdapter.getSelect();
                if (folderParam.equals(allPhotos))
                    folderParam = "";
                intent.putExtra(PhotoPickDetailActivity.FOLDER_NAME, folderParam);
                startActivityForResult(intent, REQUEST_PHOTO_DETAIL);
            }
        }
    };

    private void initActionbar() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle("选择图片");
//        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void updatePickCount() {

        if (mPhotoMode == MODE_MUTIL_CROP) {
            String format = "完成(%d/%d)";
            tvRight.setText(String.format(format, mPickData.size(), mMaxPick));
//            mMenuItem.setTitle(String.format(format, mPickData.size(), mMaxPick));
            String formatPreview = "预览(%d/%d)";
            mPreView.setText(String.format(formatPreview, mPickData.size(), mMaxPick));
        }

    }

    //判断在已选择的图片list中有没有该图片路径
    public boolean isPicked(String path) {
        for (ImageInfo item : mPickData) {
            if (item.path.equals(path)) {
                return true;
            }
        }

        return false;
    }

    //获取已选中图片显示的数字
    public int getPickedNum(String path) {
        for (ImageInfo item : mPickData) {
            if (item.path.equals(path)) {
                return item.getCheckedNum();
            }
        }

        return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (mPhotoMode == MODE_MUTIL_CROP) {
//            getMenuInflater().inflate(R.menu.menu_photo_pick, menu);
//            mMenuItem = menu.getItem(0);
//            updatePickCount();
//            return true;
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_finish) {//图片选择确定
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT_PHOTO_LIST, mPickData);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /*
     * 选择了listview的第一个项，gridview的第一个是照相机
     */
    private boolean isAllPhotoMode() {
        return mFolderId == 0;
    }

    private String[] projection = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String where;
        //选的不是第一个项目（指全部图片）
        if (!isAllPhotoMode()) {
            String select = ((FolderAdapter) mRecycleTitle.getAdapter()).getSelect();
            where = String.format("%s='%s'",
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    select);
        } else {
            where = "";
        }

        return new CursorLoader(PhotoPickActivity.this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                where,
                null,
                MediaStore.MediaColumns.DATE_ADDED + " DESC");
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (isAllPhotoMode()) {
            photoAdapter = new AllPhotoAdapter(this, data, false, this);
        } else {
            photoAdapter = new GridPhotoAdapter(this, data, false, this);
        }
        mRecycleItem.setAdapter(photoAdapter);
//        photoAdapter.setOnItemClick(mOnPhotoItemClick);
    }

    public void clickPhotoItem(String path) {

        if (mPhotoMode == MODE_SINGLE_CROP) {
            Uri uri = Uri.parse(path);
            fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
            cropImageUri(uri, fileCropUri, 640, 640, REQUEST_RESULT_PHOTO_CROP);
        }
    }

    public int getDeleteNum() {
        return deleteNum;
    }

    public static int deleteNum;

    public void clickPhotoCheck(View v) {
        GridViewCheckTag tag = (GridViewCheckTag) v.getTag();
        if (((CheckBox) v).isChecked()) {
            if (mPickData.size() >= mMaxPick) {
                ((CheckBox) v).setChecked(false);
                String s = String.format("最多只能选择%d张", mMaxPick);
                Toast.makeText(this, s, Toast.LENGTH_LONG).show();
                return;
            }
            addPicked(tag.path);
            tag.iconFore.setVisibility(View.VISIBLE);
            //设置当前选择图片数
            deleteNum = 0;
            tag.num = mPickData.size();
        } else {
            //设置没有选中
            tag.num = 0;
            removePicked(tag.path);
            tag.iconFore.setVisibility(View.INVISIBLE);
        }
        ((BaseAdapter) mRecycleItem.getAdapter()).notifyDataSetChanged();

        updatePickCount();
    }

    private void removePicked(String path) {
        int deleteIndex = -1;
        for (int i = 0; i < mPickData.size(); ++i) {
            if (mPickData.get(i).path.equals(path)) {
                deleteNum = mPickData.get(i).checkedNum;
                mPickData.get(i).checkedNum = 0;
                deleteIndex = i;
            } else {
                //设置显示数字
                if (mPickData.get(i).getCheckedNum() > deleteNum && deleteNum > 0) {
                    mPickData.get(i).setCheckedNum(mPickData.get(i).getCheckedNum() - 1);
                }
            }
        }
        if (deleteIndex > -1) {
            mPickData.remove(deleteIndex);
        }

    }

    private void addPicked(String path) {
        if (!isPicked(path)) {
            ImageInfo info = new ImageInfo(path);
            info.setCheckedNum(mPickData.size() + 1);
            mPickData.add(info);
        }
    }

    public void camera() {
        if (mPickData.size() >= mMaxPick) {
            Toast.makeText(PhotoPickActivity.this, String.format("最多只能选择%s张", mMaxPick), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = CameraPhotoUtil.getOutputMediaFileUri();
        String filePath = CameraPhotoUtil.getPath(PhotoPickActivity.this, fileUri);
        File file = new File(filePath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, REQUEST_RESULT_PHOTO);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public int getPhotoMode() {
        return mPhotoMode;
    }

    /**
     * 启动图片裁剪工具
     *
     * @param uri         原图片Uri
     * @param outputUri   裁剪后图片uri
     * @param outputX     水平长度
     * @param outputY     垂直长度
     * @param requestCode 请求码
     */
    public void cropImageUri(Uri uri, Uri outputUri, int outputX, int outputY, int requestCode) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", outputX);
            intent.putExtra("outputY", outputY);
            intent.putExtra("scale", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra("return-data", false);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
            intent.putExtra("noFaceDetection", true);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESULT_PHOTO_CROP) {//裁剪图片回调
            if (resultCode == Activity.RESULT_OK) {
                String filePath = CameraPhotoUtil.getPath(this, fileCropUri);
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT_CROP_PHOTO, filePath);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        } else if (requestCode == REQUEST_RESULT_PHOTO) {//接受系统拍照回调
            if (resultCode == Activity.RESULT_OK) {
                //单图片模式启动裁剪工具
                if (mPhotoMode == MODE_SINGLE_CROP) {
                    Log.d("unlock", "uri.getPath()" + (fileUri == null));//这里有时会为空
                    String filePath = CameraPhotoUtil.getPath(this, fileUri);
                    File file = new File(filePath);
                    fileCropUri = CameraPhotoUtil.getOutputMediaFileUri();
                    cropImageUri(fileUri, fileCropUri, 640, 640, REQUEST_RESULT_PHOTO_CROP);
                }
                //多图片时将它和之前选择的图片都返回
                else {
                    String filePath = CameraPhotoUtil.getPath(this, fileUri);
                    ImageInfo imageInfo = new ImageInfo(filePath);
                    mPickData.add(imageInfo);
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_RESULT_PHOTO_LIST, mPickData);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        } else if (requestCode == REQUEST_PHOTO_DETAIL) {//图片详情页面回调
            if (resultCode == Activity.RESULT_OK) {
                boolean send = data.getBooleanExtra(PhotoPickDetailActivity.RESULT_SEND, false);
                ArrayList<ImageInfo> imageInfos = (ArrayList<ImageInfo>) data.getSerializableExtra(PhotoPickDetailActivity.RESULT_DATA);
                if (imageInfos != null && imageInfos.size() > 0) {
                    mPickData.clear();
                    mPickData.addAll(imageInfos);
                    updatePickCount();
                    photoAdapter.notifyDataSetChanged();
                }
                if (send) {
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_RESULT_PHOTO_LIST, mPickData);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        }
    }

    static class GridViewCheckTag {
        View iconFore;
        String path = "";
        int num;

        GridViewCheckTag(View iconFore) {
            this.iconFore = iconFore;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LruMemory.getInstance().trimToSize(-1);
    }
}
