package com.fangbangbang.fbb.widget.imageselector;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;

/**
 * 作者：Rimon on 2017/5/28 17:46
 */
public class ImageSelector {
    public static final int IMAGE_REQUEST_CODE = 1002;
    public static final int IMAGE_CROP_CODE = 1003;

    private static ImgSelConfig mImageConfig;

    public static ImgSelConfig getImageConfig() {
        return mImageConfig;
    }

    public static void open(Activity activity, ImgSelConfig config, int RequestCode) {
        if (config == null) {
            return;
        }
        mImageConfig = config;

        Intent intent = new Intent(activity, ImgSelActivity.class);
        activity.startActivityForResult(intent, RequestCode);
    }

    public static void open(Fragment fragment, ImgSelConfig config, int RequestCode) {
        if (config == null) {
            return;
        }
        mImageConfig = config;
        Intent intent = new Intent(fragment.getActivity(), ImgSelActivity.class);
        fragment.startActivityForResult(intent, RequestCode);
    }
}
