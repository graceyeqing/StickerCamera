package com.stickercamera.app.camera.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.common.util.FileUtils;
import com.common.util.ImageUtils;
import com.common.util.StringUtils;
import com.common.util.TimeUtils;
import com.customview.LabelSelector;
import com.customview.LabelView;
import com.customview.MyHighlightView;
import com.customview.MyImageViewDrawableOverlay;
import com.github.skykai.stickercamera.R;
import com.stickercamera.App;
import com.stickercamera.AppConstants;
import com.stickercamera.app.camera.CameraBaseActivity;
import com.stickercamera.app.camera.CameraManager;
import com.stickercamera.app.camera.EffectService;
import com.stickercamera.app.camera.adapter.FilterAdapter;
import com.stickercamera.app.camera.adapter.GetImgAdapter;
import com.stickercamera.app.camera.adapter.StickerTagAdapter;
import com.stickercamera.app.camera.adapter.StickerToolAdapter;
import com.stickercamera.app.camera.effect.FilterEffect;
import com.stickercamera.app.camera.util.EffectUtil;
import com.stickercamera.app.camera.util.GPUImageFilterTools;
import com.stickercamera.app.model.Addon;
import com.stickercamera.app.model.FeedItem;
import com.stickercamera.app.model.TagItem;
import com.stickercamera.app.ui.EditTextActivity;
import com.stickercamera.photopick.ImageInfo;
import com.stickercamera.photopick.PhotoPickActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

import static com.stickercamera.photopick.PhotoPickActivity.EXTRA_RESULT_PHOTO_LIST;
import static com.stickercamera.photopick.PhotoPickActivity.MODE_MUTIL_CROP;
import static com.stickercamera.photopick.PhotoPickActivity.REQUEST_RESULT_PHOTO;

/**
 * 图片处理界面
 * Created by sky on 2015/7/8.
 * Weibo: http://weibo.com/2030683111
 * Email: 1132234509@qq.com
 */
public class PhotoProcessActivity extends CameraBaseActivity {

    //滤镜图片
    @InjectView(R.id.gpuimage)
    GPUImageView mGPUImageView;
    //绘图区域
    @InjectView(R.id.drawing_view_container)
    ViewGroup drawArea;
    //底部按钮
    @InjectView(R.id.sticker_btn)
    TextView stickerBtn;
    @InjectView(R.id.filter_btn)
    TextView filterBtn;
    @InjectView(R.id.text_btn)
    TextView labelBtn;
    //工具区
    @InjectView(R.id.list_tools)
    HListView bottomToolBar;//贴纸，滤镜列表
    @InjectView(R.id.toolbar_area)
    ViewGroup toolArea;
    @InjectView(R.id.list_tags)
    HListView mListTags;//贴纸标签
    HListView mListImages;//顶部小图展示列表
    private ImageView tvLeft;
    private TextView tvRight;
    private MyImageViewDrawableOverlay mImageView;//贴纸的叠加层
    private LabelSelector labelSelector;

    //当前选择底部按钮
    private TextView currentBtn;
    //当前图片
    private Bitmap currentBitmap;
    //用于预览的小图片
    private Bitmap smallImageBackgroud;
    //小白点标签
    private LabelView emptyLabelView;

    private List<LabelView> labels = new ArrayList<LabelView>();
    private List<FilterEffect> filters = EffectService.getInst().getLocalFilters();//滤镜列表
    //标签区域
    private View commonLabelArea;

    private ArrayList<ImageInfo> mPickData;//选取的图片
    private GetImgAdapter mGetImgAdapter;//小图adapter
    private FilterAdapter filterAdapter;//滤镜adapter
    private int prePickPosition;//前一张处理的图片
    private int currentPickPosition;//当前处理的图片
    private ArrayList<FeedItem> mSavePickData = new ArrayList<>();//保存的图片
    private List<List<MyHighlightView>> hightlistViews = new ArrayList<>();
    private boolean isNext;//点击下一步

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_process);
        ButterKnife.inject(this);
        EffectUtil.clear();
        initView();
        initEvent();
        initStickerToolBar();
        initStickerTagBar();

        //获取选中的所有图片
        mPickData = (ArrayList<ImageInfo>) getIntent().getSerializableExtra("DATA");
        if (mPickData != null && mPickData.size() > 0) {
            loadImage(Uri.parse(mPickData.get(0).getPath()));
        } else {
            mPickData = new ArrayList<>();
            ImageInfo image = new ImageInfo(getIntent().getData().getPath());
            mPickData.add(image);
            loadImage(getIntent().getData());
        }
        //初始化贴纸列表
        for (int i = 0; i < mPickData.size(); i++) {
            List<MyHighlightView> itemlistViews = new CopyOnWriteArrayList<MyHighlightView>();
            hightlistViews.add(itemlistViews);
        }
        //标题栏选中图缩略图展示
        mGetImgAdapter = new GetImgAdapter(this, mPickData);
        mListImages.setAdapter(mGetImgAdapter);
        mListImages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < mPickData.size()) {
                    //保存前一张图片
                    showProgressDialog("");
                    isNext = false;
                    savePicture();
                    prePickPosition = currentPickPosition;
                    currentPickPosition = position;

                    mGetImgAdapter.setClickPosition(position);
                    mGetImgAdapter.notifyDataSetChanged();

                    //移除前一个图上的标签
                    if (mPickData.get(prePickPosition).getLabels() != null
                            && mPickData.get(prePickPosition).getLabels().size() > 0) {
                        for (LabelView item : mPickData.get(prePickPosition).getLabels()) {
                            EffectUtil.removeLabelEditable(mImageView, drawArea, item);
                        }
                    }
                    //移除前一个图上的所有贴纸和标签
                    mImageView.clearOverlays();

                    //加载当前图上的贴纸
                    if (mPickData.get(currentPickPosition).getStickers() != null
                            && mPickData.get(currentPickPosition).getStickers().size() > 0) {
                        for (int i = 0; i < mPickData.get(currentPickPosition).getStickers().size(); i++) {
                            Addon item = mPickData.get(currentPickPosition).getStickers().get(i);
                            addStickerImage(item, false, hightlistViews.get(currentPickPosition).get(i).getMatrix(),
                                    hightlistViews.get(currentPickPosition).get(i).getRotation(),
                                    hightlistViews.get(currentPickPosition).get(i).getCropRectF());
                        }
                    }

                    //加载当前图上的标签
                    if (mPickData.get(currentPickPosition).getLabels() != null
                            && mPickData.get(currentPickPosition).getLabels().size() > 0) {
                        for (LabelView item : mPickData.get(currentPickPosition).getLabels()) {
                            EffectUtil.addLabelEditable(mImageView, drawArea, item, item.getLeft(), item.getTop());
                        }
                    }

                    //加载当前的滤镜
                    GPUImageFilter filter = GPUImageFilterTools.createFilterForType(
                            PhotoProcessActivity.this, filters.get(mPickData.get(currentPickPosition).getFilterId()).getType());
                    mGPUImageView.setFilter(filter);

                    loadImage(Uri.parse(mPickData.get(position).getPath()));

                } else {
                    //从手机相册选择图片
                    Intent intent = new Intent(PhotoProcessActivity.this, PhotoPickActivity.class);
                    intent.putExtra(PhotoPickActivity.EXTRA_MODE, MODE_MUTIL_CROP);
                    intent.putExtra(PhotoPickActivity.EXTRA_MAX, 9 - mPickData.size());
                    startActivityForResult(intent, PhotoPickActivity.REQUEST_RESULT_PHOTO);
                }
            }
        });

    }

    private void loadImage(Uri imageUri) {

        ImageUtils.asyncLoadImage(this, imageUri, new ImageUtils.LoadImageCallback() {
            @Override
            public void callback(Bitmap result) {
                if (result != null) {
//                    mGPUImageView.resetImage();
                    try {
                        mGPUImageView.getGPUImage().deleteImage();
                    }catch (Exception e){}
                    currentBitmap = result;
                    mGPUImageView.setImage(currentBitmap);
                }

            }
        });

        ImageUtils.asyncLoadSmallImage(this, imageUri, new ImageUtils.LoadImageCallback() {
            @Override
            public void callback(Bitmap result) {
                if (result != null) {
                    smallImageBackgroud = result;
                    //设置滤镜小图
                    if (currentBtn == filterBtn) {
                        mListTags.setVisibility(View.GONE);
                        bottomToolBar.setVisibility(View.VISIBLE);
                        labelSelector.hide();
                        emptyLabelView.setVisibility(View.INVISIBLE);
                        commonLabelArea.setVisibility(View.GONE);
                        initFilterToolBar();
                    }
                }
            }
        });
    }

    private void initView() {
        //添加贴纸水印的画布
        mImageView = new MyImageViewDrawableOverlay(this);
        View overlay = LayoutInflater.from(PhotoProcessActivity.this).inflate(
                R.layout.view_drawable_overlay, null);
        mImageView = (MyImageViewDrawableOverlay) overlay.findViewById(R.id.drawable_overlay);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(App.getApp().getScreenWidth(),
                App.getApp().getScreenWidth());
        mImageView.setLayoutParams(params);
        overlay.setLayoutParams(params);
        drawArea.addView(overlay);
        //添加标签选择器
        RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams(App.getApp().getScreenWidth(), App.getApp().getScreenWidth());
        labelSelector = new LabelSelector(this);
        labelSelector.setLayoutParams(rparams);
        drawArea.addView(labelSelector);
        labelSelector.hide();

        //初始化滤镜图片
        mGPUImageView.setLayoutParams(rparams);
        //初始化空白标签
        emptyLabelView = new LabelView(this);
        emptyLabelView.setEmpty();
        EffectUtil.addLabelEditable(mImageView, drawArea, emptyLabelView,
                mImageView.getWidth() / 2, mImageView.getWidth() / 2);
        emptyLabelView.setVisibility(View.INVISIBLE);

        //初始化推荐标签栏
        commonLabelArea = LayoutInflater.from(PhotoProcessActivity.this).inflate(
                R.layout.view_label_bottom, null);
        commonLabelArea.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        toolArea.addView(commonLabelArea);
        commonLabelArea.setVisibility(View.GONE);

        //头部标题栏布局
        mListImages = (HListView) findViewById(R.id.include_title).findViewById(R.id.list_images);
        tvLeft = (ImageView) findViewById(R.id.include_title).findViewById(R.id.base_title_tv_left);
        tvRight = (TextView) findViewById(R.id.include_title).findViewById(R.id.base_title_tv_right);
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNext = true;
                savePicture();
            }
        });
    }

    private void initEvent() {
        stickerBtn.setOnClickListener(v -> {
            if (!setCurrentBtn(stickerBtn)) {
                return;
            }
            mListTags.setVisibility(View.VISIBLE);
            bottomToolBar.setVisibility(View.VISIBLE);
            labelSelector.hide();
            emptyLabelView.setVisibility(View.GONE);
            commonLabelArea.setVisibility(View.GONE);
            initStickerToolBar();
        });

        filterBtn.setOnClickListener(v -> {
            if (!setCurrentBtn(filterBtn)) {
                return;
            }
            mListTags.setVisibility(View.GONE);
            bottomToolBar.setVisibility(View.VISIBLE);
            labelSelector.hide();
            emptyLabelView.setVisibility(View.INVISIBLE);
            commonLabelArea.setVisibility(View.GONE);
            initFilterToolBar();
        });
        labelBtn.setOnClickListener(v -> {
            if (!setCurrentBtn(labelBtn)) {
                return;
            }
            mListTags.setVisibility(View.GONE);
            bottomToolBar.setVisibility(View.GONE);
            labelSelector.showToTop();
            commonLabelArea.setVisibility(View.VISIBLE);

        });
        labelSelector.setTxtClicked(v -> {
            EditTextActivity.openTextEdit(PhotoProcessActivity.this, "", 8, AppConstants.ACTION_EDIT_LABEL);
        });
        labelSelector.setAddrClicked(v -> {
            EditTextActivity.openTextEdit(PhotoProcessActivity.this, "", 8, AppConstants.ACTION_EDIT_LABEL_POI);

        });
        mImageView.setOnDrawableEventListener(wpEditListener);
        mImageView.setSingleTapListener(() -> {
            //单击，出现心情和地址标签
            if (currentBtn == labelBtn) {
                emptyLabelView.updateLocation((int) mImageView.getmLastMotionScrollX(),
                        (int) mImageView.getmLastMotionScrollY());
                emptyLabelView.setVisibility(View.VISIBLE);
                labelSelector.showToTop();
                drawArea.postInvalidate();
            }
        });
        labelSelector.setOnClickListener(v -> {
            labelSelector.hide();
            emptyLabelView.updateLocation((int) labelSelector.getmLastTouchX(),
                    (int) labelSelector.getmLastTouchY());
            emptyLabelView.setVisibility(View.VISIBLE);
        });


//        titleBar.setRightBtnOnclickListener(v -> {
//            savePicture();
//        });
    }

    //保存图片
    private void savePicture() {
        //加滤镜
        final Bitmap newBitmap = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(newBitmap);
        RectF dst = new RectF(0, 0, mImageView.getWidth(), mImageView.getHeight());
        try {
            cv.drawBitmap(mGPUImageView.capture(), null, dst, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
            cv.drawBitmap(currentBitmap, null, dst, null);
        }
        //加贴纸水印
        EffectUtil.applyOnSave(cv, mImageView, hightlistViews.get(currentPickPosition),
                mPickData.get(currentPickPosition).getLabels());

        new SavePicToFileTask().execute(newBitmap);
    }

    private class SavePicToFileTask extends AsyncTask<Bitmap, Void, String> {
        Bitmap bitmap;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isNext) {
                showProgressDialog("图片处理中...");
            }
        }

        @Override
        protected String doInBackground(Bitmap... params) {
            String fileName = null;
            try {
                bitmap = params[0];

                String picName = TimeUtils.dtFormat(new Date(), "yyyyMMddHHmmss");
                //保存到文件夹
                fileName = ImageUtils.saveToFile(FileUtils.getInst().getPhotoSavedPath() + "/" + picName, false, bitmap);
                //保存到相册
                ImageUtils.saveImageToDICM(PhotoProcessActivity.this, picName, bitmap);

            } catch (Exception e) {
                e.printStackTrace();
                toast("图片处理错误，请退出相机并重试", Toast.LENGTH_LONG);
            }
            return fileName;
        }

        @Override
        protected void onPostExecute(String fileName) {
            super.onPostExecute(fileName);
            dismissProgressDialog();
            if (StringUtils.isEmpty(fileName)) {
                return;
            }
            //保存标签信息
            List<TagItem> tagInfoList = new ArrayList<TagItem>();
            for (LabelView label : labels) {
                tagInfoList.add(label.getTagInfo());
            }

            FeedItem feedItem = new FeedItem(tagInfoList, fileName, mPickData.size());
            mSavePickData.add(feedItem);
            //将图片信息通过EventBus发送到MainActivity
            if (isNext) {
//                EventBus.getDefault().post(feedItem);
                EventBus.getDefault().post(mSavePickData);
                CameraManager.getInst().close();
            }

        }
    }


    public void tagClick(View v) {
        TextView textView = (TextView) v;
        TagItem tagItem = new TagItem(AppConstants.POST_TYPE_TAG, textView.getText().toString());
        addLabel(tagItem);
    }

    private MyImageViewDrawableOverlay.OnDrawableEventListener wpEditListener = new MyImageViewDrawableOverlay.OnDrawableEventListener() {
        @Override
        public void onMove(MyHighlightView view) {
        }

        @Override
        public void onFocusChange(MyHighlightView newFocus, MyHighlightView oldFocus) {
        }

        @Override
        public void onDown(MyHighlightView view) {

        }

        @Override
        public void onClick(MyHighlightView view) {
            labelSelector.hide();
        }

        @Override
        public void onClick(final LabelView label) {
            if (label.equals(emptyLabelView)) {
                return;
            }
            alert("温馨提示", "是否需要删除该标签！", "确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EffectUtil.removeLabelEditable(mImageView, drawArea, label);
                    mPickData.get(currentPickPosition).getLabels().remove(label);
                }
            }, "取消", null);
        }
    };

    private boolean setCurrentBtn(TextView btn) {
        if (currentBtn == null) {
            currentBtn = btn;
        } else if (currentBtn.equals(btn)) {
            return false;
        } else {
            currentBtn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
        Drawable myImage = getResources().getDrawable(R.drawable.select_icon);
        btn.setCompoundDrawablesWithIntrinsicBounds(null, null, null, myImage);
        currentBtn = btn;
        return true;
    }


    //初始化贴图
    private void initStickerToolBar() {

        bottomToolBar.setAdapter(new StickerToolAdapter(PhotoProcessActivity.this, EffectUtil.addonList));
        bottomToolBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0,
                                    View arg1, int arg2, long arg3) {
                labelSelector.hide();
                Addon sticker = EffectUtil.addonList.get(arg2);
                addStickerImage(sticker, true);
            }
        });
        setCurrentBtn(stickerBtn);
    }

    //添加贴纸
    private void addStickerImage(Addon sticker, boolean isNew) {
        MyHighlightView hv = EffectUtil.addStickerImage(mImageView, PhotoProcessActivity.this, sticker,
                new EffectUtil.StickerCallback() {
                    @Override
                    public void onRemoveSticker(Addon sticker) {
                        labelSelector.hide();
                        mPickData.get(currentPickPosition).getStickers().remove(sticker);
                    }
                });
        if (isNew) {
            mPickData.get(currentPickPosition).getStickers().add(sticker);
            hightlistViews.get(currentPickPosition).add(hv);
        }
    }

    private void addStickerImage(Addon sticker, boolean isNew, Matrix m, float rotateM, RectF cropRect) {
        EffectUtil.addStickerImage(mImageView, PhotoProcessActivity.this, sticker,
                m, rotateM, cropRect,
                new EffectUtil.StickerCallback() {
                    @Override
                    public void onRemoveSticker(Addon sticker) {
                        labelSelector.hide();
                        mPickData.get(currentPickPosition).getStickers().remove(sticker);
                    }
                });
    }

    //初始化贴纸标签类型
    private void initStickerTagBar() {
        List<String> tags = new ArrayList<>();
        tags.add("热门贴纸");
        tags.add("吃货日常");
        tags.add("文艺范");
        tags.add("在路上");
        tags.add("标题党");
        StickerTagAdapter tagAdapter = new StickerTagAdapter(PhotoProcessActivity.this, tags);
        mListTags.setAdapter(tagAdapter);
        mListTags.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0,
                                    View arg1, int arg2, long arg3) {
                tagAdapter.setCurrentTag(arg2);
                tagAdapter.notifyDataSetChanged();
            }
        });
    }


    //初始化滤镜
    private void initFilterToolBar() {

        filterAdapter = new FilterAdapter(PhotoProcessActivity.this, filters, smallImageBackgroud);
        bottomToolBar.setAdapter(filterAdapter);
        filterAdapter.setSelectFilter(mPickData.get(currentPickPosition).getFilterId());
        if (mPickData.get(currentPickPosition).getFilterId() > 3) {
            bottomToolBar.setSelection(mPickData.get(currentPickPosition).getFilterId() - 2);
        }
        bottomToolBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                labelSelector.hide();
                if (filterAdapter.getSelectFilter() != arg2) {
                    filterAdapter.setSelectFilter(arg2);
                    mPickData.get(currentPickPosition).setFilterId(arg2);
                    filterAdapter.notifyDataSetChanged();
                    GPUImageFilter filter = GPUImageFilterTools.createFilterForType(
                            PhotoProcessActivity.this, filters.get(arg2).getType());
                    mGPUImageView.setFilter(filter);
                    GPUImageFilterTools.FilterAdjuster mFilterAdjuster = new GPUImageFilterTools.FilterAdjuster(filter);
                    //可调节颜色的滤镜
                    if (mFilterAdjuster.canAdjust()) {
                        //mFilterAdjuster.adjust(100); 给可调节的滤镜选一个合适的值
                    }
                }
            }
        });
    }

    //添加标签
    private void addLabel(TagItem tagItem) {
        labelSelector.hide();
        emptyLabelView.setVisibility(View.INVISIBLE);
//        if (labels.size() >= 5) {
//            alert("温馨提示", "您只能添加5个标签！", "确定", null, null, null, true);
//        } else {
//            int left = emptyLabelView.getLeft();
//            int top = emptyLabelView.getTop();
//            if (labels.size() == 0 && left == 0 && top == 0) {
//                left = mImageView.getWidth() / 2 - 10;
//                top = mImageView.getWidth() / 2;
//            }
//            LabelView label = new LabelView(PhotoProcessActivity.this);
//            label.init(tagItem);
//            EffectUtil.addLabelEditable(mImageView, drawArea, label, left, top);
//            labels.add(label);
//        }

        if (mPickData.get(currentPickPosition).getLabels().size() >= 5) {
            alert("温馨提示", "您只能添加5个标签！", "确定", null, null, null, true);
        } else {
            int left = emptyLabelView.getLeft();
            int top = emptyLabelView.getTop();
            if (mPickData.get(currentPickPosition).getLabels().size() == 0 && left == 0 && top == 0) {
                left = mImageView.getWidth() / 2 - 10;
                top = mImageView.getWidth() / 2;
            }
            LabelView label = new LabelView(PhotoProcessActivity.this);
            label.init(tagItem);
            EffectUtil.addLabelEditable(mImageView, drawArea, label, left, top);
            mPickData.get(currentPickPosition).getLabels().add(label);
            if (currentPickPosition == 0) {
                labels.add(label);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        labelSelector.hide();
        super.onActivityResult(requestCode, resultCode, data);
        if (AppConstants.ACTION_EDIT_LABEL == requestCode && data != null) {
            String text = data.getStringExtra(AppConstants.PARAM_EDIT_TEXT);
            if (StringUtils.isNotEmpty(text)) {
                TagItem tagItem = new TagItem(AppConstants.POST_TYPE_TAG, text);
                addLabel(tagItem);
            }
        } else if (AppConstants.ACTION_EDIT_LABEL_POI == requestCode && data != null) {
            String text = data.getStringExtra(AppConstants.PARAM_EDIT_TEXT);
            if (StringUtils.isNotEmpty(text)) {
                TagItem tagItem = new TagItem(AppConstants.POST_TYPE_POI, text);
                addLabel(tagItem);
            }
        }

        if (requestCode == REQUEST_RESULT_PHOTO) {//接受系统拍照回调
            if (resultCode == Activity.RESULT_OK) {
                //返回选取图片
                ArrayList<ImageInfo> pickList = (ArrayList<ImageInfo>) data.getSerializableExtra(EXTRA_RESULT_PHOTO_LIST);
                if (pickList != null && pickList.size() > 0) {
                    mPickData.addAll(pickList);
                    //添加贴纸列表
                    for (int i = 0; i < pickList.size(); i++) {
                        List<MyHighlightView> itemlistViews = new CopyOnWriteArrayList<MyHighlightView>();
                        hightlistViews.add(itemlistViews);
                    }
                    mGetImgAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPickData != null) {
            mPickData = null;
        }
        if (currentBitmap != null) {
            currentBitmap.recycle();
            currentBitmap = null;
        }
        if (smallImageBackgroud != null) {
            smallImageBackgroud.recycle();
            smallImageBackgroud = null;
        }

        if (hightlistViews != null) {
            hightlistViews = null;
        }

        if (mSavePickData != null) {
            mSavePickData = null;
        }
    }

}
