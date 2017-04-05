package com.stickercamera.photopick;


import com.customview.LabelView;
import com.stickercamera.app.model.Addon;
import com.stickercamera.app.model.BaseBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tian on 15-9-9.
 */
public class ImageInfo extends BaseBean {

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String path;
    public long photoId;
    public int width;
    public int height;
    public int checkedNum;//选中的显示的数字
    private List<Addon> stickers = new ArrayList<>();//添加的贴纸列表
    private List<LabelView> labels = new ArrayList<>();//标签列表
    public int filterId;//添加的滤镜id
    private static final String prefix = "file://";

    public ImageInfo(String path) {
        this.path = pathAddPreFix(path);
    }

    public static String pathAddPreFix(String path) {
        if (!path.startsWith(prefix)) {
            path = prefix + path;
        }
        return path;
    }

    public List<Addon> getStickers() {
        return stickers;
    }

    public void setStickers(List<Addon> stickers) {
        this.stickers = stickers;
    }

    public int getFilterId() {
        return filterId;
    }

    public void setFilterId(int filterId) {
        this.filterId = filterId;
    }

    public int getCheckedNum() {
        return checkedNum;
    }

    public void setCheckedNum(int checkedNum) {
        this.checkedNum = checkedNum;
    }

    public List<LabelView> getLabels() {
        return labels;
    }

    public void setLabels(List<LabelView> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "ImageInfo{" +
                "path='" + path + '\'' +
                ", photoId=" + photoId +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
