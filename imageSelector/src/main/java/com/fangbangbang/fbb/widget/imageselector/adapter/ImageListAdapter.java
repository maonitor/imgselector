package com.fangbangbang.fbb.widget.imageselector.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;


import com.fangbangbang.fbb.widget.imageselector.EasyRVHolder;
import com.fangbangbang.fbb.widget.imageselector.ImgSelConfig;
import com.fangbangbang.fbb.widget.imageselector.R;
import com.fangbangbang.fbb.widget.imageselector.bean.Image;
import com.fangbangbang.fbb.widget.imageselector.common.OnItemClickListener;
import com.fangbangbang.fbb.widget.imageselector.widget.SquareRelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class ImageListAdapter extends EasyRVAdapter<Image> {

    private boolean showCamera;
    private boolean multiSelect;

    private ImgSelConfig config;
    private Context context;
    private OnItemClickListener listener;
    private ArrayList<String> selectImageList = new ArrayList<>();

    public ImageListAdapter(Context context, List<Image> list, ImgSelConfig config) {
        super(context, list, R.layout.img_sel_item, R.layout.img_sel_item_take_photo);
        this.context = context;
        this.config = config;
        this.selectImageList = config.originImagePath;
    }

    @Override
    protected void onBindData(final EasyRVHolder viewHolder, final int position, final Image item) {
        if (position == 0 && showCamera) {
            ImageView iv = viewHolder.getView(R.id.ivTakePhoto);
            SquareRelativeLayout relativeLayout = viewHolder.getView(R.id.take_photo_layout);
            iv.setImageResource(R.drawable.ic_take_photo);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onImageClick(position, item);
                }
            });
            return;
        }

        if (multiSelect) {
            viewHolder.getView(R.id.ivPhotoCheaked).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int ret = listener.onCheckedClick(position, item);
                        if (ret == 1) { // 局部刷新
                            if (selectImageList.contains(item.path)) {
                                viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_checked);
                            } else {
                                viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_uncheck);
                            }
                        }
                    }
                }
            });
        }

        viewHolder.setOnItemViewClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null)
                    listener.onImageClick(position, item);
            }
        });

        final ImageView iv = viewHolder.getView(R.id.ivImage);
        config.loader.displayImage(context, item.path, iv);

        if (multiSelect) {
            viewHolder.setVisible(R.id.ivPhotoCheaked, true);
            if (selectImageList.contains(item.path)) {
                viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_checked);
            } else {
                viewHolder.setImageResource(R.id.ivPhotoCheaked, R.drawable.ic_uncheck);
            }
        } else {
            viewHolder.setVisible(R.id.ivPhotoCheaked, false);
        }
    }

    public void setShowCamera(boolean showCamera) {
        this.showCamera = showCamera;
    }

    public void setMultiSelect(boolean mutiSelect) {
        this.multiSelect = mutiSelect;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && showCamera) {
            return 1;
        }
        return 0;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
