package com.fangbangbang.fbb.widget.imageselector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.fangbangbang.fbb.widget.imageselector.common.Callback;
import com.fangbangbang.fbb.widget.imageselector.utils.FileUtils;
import com.fangbangbang.fbb.widget.imageselector.utils.Platform;
import com.fangbangbang.fbb.widget.imageselector.utils.StatusBarCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;


public class ImgSelActivity extends FragmentActivity implements View.OnClickListener, Callback {
    private final String TAG = "ImgSelActivity";
    public static final String INTENT_RESULT = "result";
    public static final String INTENT_RESULT_URI = "result_uri";
    private static final int IMAGE_CROP_CODE = 1;
    private static final int STORAGE_REQUEST_CODE = 1;

    private ImgSelConfig config;

    private TextView mTvTitle;
    private Button mBtnConfirm;
    private ImageView mIvBack;
    private Uri mCropImageUri;

    private ImgSelFragment fragment;
    private ArrayList<String> mSelectImagePathList = new ArrayList<>();
    private ArrayList<String> mBackPathList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_sel_activity);
        config = ImageSelector.getImageConfig();
        mSelectImagePathList = config.originImagePath;
        mBackPathList.addAll(mSelectImagePathList);

        // Android 6.0 checkSelfPermission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
        } else {
            fragment = ImgSelFragment.instance();
            getSupportFragmentManager().beginTransaction().add(R.id.fl_image_list, fragment, null).commit();
        }

        initView();
        if (!FileUtils.isSdCardAvailable()) {
            Toast.makeText(ImgSelActivity.this, getString(R.string.sd_disable), Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        Toolbar mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mBtnConfirm.setOnClickListener(this);

        if (config != null) {
            if (config.backResId != -1) {
                mToolBar.setNavigationIcon(config.backResId);
            }
            if (config.statusBarColor != -1) {
                StatusBarCompat.compat(this, config.statusBarColor);
            }
            mTvTitle.setText(config.title);
            mBtnConfirm.setBackgroundColor(config.btnBgColor);
            mBtnConfirm.setTextColor(config.btnTextColor);

            if (config.multiSelect) {
                if (!config.rememberSelected) {
                    mSelectImagePathList.clear();
                }
                mBtnConfirm.setText(String.format(getString(R.string.confirm_format), config.btnText, mSelectImagePathList.size(), config.maxNum));
            } else {
                mSelectImagePathList.clear();
                mBtnConfirm.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnConfirm) {
            exit();
        }
    }

    @Override
    public void onSingleImageSelected(String path) {
        if (config.needCrop) {
            crop(Uri.parse(path));
        } else {
            mSelectImagePathList.add(path);
            exit();
        }
    }

    @Override
    public void onImageSelected(String path) {
        if (!mSelectImagePathList.contains(path)) {
            mSelectImagePathList.add(path);
        }
        mBtnConfirm.setText(String.format(getString(R.string.confirm_format), config.btnText, mSelectImagePathList.size(), config.maxNum));
    }

    @Override
    public void onImageUnselected(String path) {
        mSelectImagePathList.remove(path);
        mBtnConfirm.setText(String.format(getString(R.string.confirm_format), config.btnText, mSelectImagePathList.size(), config.maxNum));
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            Uri uri = Uri.parse(imageFile.getAbsolutePath());
            if (config.needCrop) {
                crop(uri);
            } else {
                mSelectImagePathList.add(uri.toString());
                config.multiSelect = false; // 多选点击拍照，强制更改为单选
                exit();
            }
        }
    }

    @Override
    public void onCameraShot(String path) {
        if (!TextUtils.isEmpty(path)) {
            if (config.needCrop) {
                crop(Uri.parse(path));
            } else {
                mSelectImagePathList.add(path);
                config.multiSelect = false; // 多选点击拍照，强制更改为单选
                exit();
            }
        }
    }

    @Override
    public void onPreviewChanged(int select, int sum, boolean visible) {
        if (visible) {
            String text = select + "/" + sum;
            mTvTitle.setText(text);
        } else {
            mTvTitle.setText(config.title);
        }
    }

    private void crop(Uri uri) {
        File file = new File(this.getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".jpg");
        mCropImageUri = FileUtils.getUri(this, file);
        Log.d("mCropImageUri", mCropImageUri.toString());
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", config.aspectX);
        intent.putExtra("aspectY", config.aspectY);
        intent.putExtra("outputX", config.outputX);
        intent.putExtra("outputY", config.outputY);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, ImageSelector.IMAGE_CROP_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageSelector.IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            mSelectImagePathList.add(mCropImageUri.toString());
            config.multiSelect = false; // 多选点击拍照，强制更改为单选
            exit();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void exit() {
        Intent intent = new Intent();
        mSelectImagePathList.removeAll(Collections.singleton(null));
        ArrayList<String> mResult = new ArrayList<>();
        ArrayList<String> mResultUri = new ArrayList<>();
        ArrayList<Uri> mResultToUri = new ArrayList<>();
        for (String path : mSelectImagePathList) {
            mResult.add(FileUtils.getUriInPath(this, path));
            mResultUri.add(path);
            mResultToUri.add(Uri.parse(path));
        }
        Log.d(TAG, "传递的参数1" + mResult.toString());
        Log.d(TAG, "传递的参数2" + mResultUri.toString());
        Log.d(TAG, "传递的参数3" + mResultToUri.toString());
        if (Platform.hasQ()) {
            intent.putStringArrayListExtra(INTENT_RESULT, mResultUri);
        } else {
            intent.putStringArrayListExtra(INTENT_RESULT, mResult);
        }
        intent.putParcelableArrayListExtra(INTENT_RESULT_URI, mResultToUri);
        setResult(RESULT_OK, intent);
        if (!config.multiSelect) {
            mSelectImagePathList.clear();
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_REQUEST_CODE:
                if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .add(R.id.fl_image_list, ImgSelFragment.instance(), null)
                            .commitAllowingStateLoss();
                } else {
                    Toast.makeText(ImgSelActivity.this, getString(R.string.permission_storage_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment == null || !fragment.hidePreview()) {
            mSelectImagePathList.clear();
            mSelectImagePathList.addAll(mBackPathList);
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
