package com.fangbangbang.fbb.widget.imageselector.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;

import com.fangbangbang.fbb.widget.imageselector.ImgSelConfig;
import com.fangbangbang.fbb.widget.imageselector.R;
import com.fangbangbang.fbb.widget.imageselector.bean.Image;
import com.fangbangbang.fbb.widget.imageselector.common.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuyh.
 * @date 2016/9/28.
 */
public class PreviewAdapter extends PagerAdapter {

    private Activity activity;
    private List<Image> images;
    private ImgSelConfig config;
    private OnItemClickListener listener;
    private ArrayList<String> selectImageList = new ArrayList<>();

    public PreviewAdapter(Activity activity, List<Image> images, ImgSelConfig config) {
        this.activity = activity;
        this.images = images;
        this.config = config;
        this.selectImageList = config.originImagePath;
    }

    @Override
    public int getCount() {
        if (config.needCamera)
            return images.size() - 1;
        else
            return images.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, final int position) {
        View root = View.inflate(activity, R.layout.img_sel_item_pager, null);
        final ImageView photoView = (ImageView) root.findViewById(R.id.ivImage);
        final ImageView ivChecked = (ImageView) root.findViewById(R.id.ivPhotoCheaked);

        if (config.multiSelect) {

            ivChecked.setVisibility(View.VISIBLE);
            final Image image = images.get(config.needCamera ? position + 1 : position);
            if (selectImageList.contains(image.path)) {
                ivChecked.setImageResource(R.drawable.ic_checked);
            } else {
                ivChecked.setImageResource(R.drawable.ic_uncheck);
            }

            ivChecked.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int ret = listener.onCheckedClick(position, image);
                        if (ret == 1) { // 局部刷新
                            if (selectImageList.contains(image.path)) {
                                ivChecked.setImageResource(R.drawable.ic_checked);
                            } else {
                                ivChecked.setImageResource(R.drawable.ic_uncheck);
                            }
                        }
                    }
                }
            });

            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onImageClick(position, images.get(position));
                    }
                }
            });
        } else {
            ivChecked.setVisibility(View.GONE);
        }

        container.addView(root, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        displayImage(photoView, images.get(config.needCamera ? position + 1 : position).path);

        return root;
    }

    private void displayImage(ImageView photoView, String path) {
        config.loader.displayImage(activity, path, photoView);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
