package com.fangbangbang.fbb.widget.imageselector.common;


import com.fangbangbang.fbb.widget.imageselector.bean.Image;

/**
 * @author yuyh.
 * @date 2016/8/5.
 */
public interface OnItemClickListener {

    int onCheckedClick(int position, Image image);

    void onImageClick(int position, Image image);
}
