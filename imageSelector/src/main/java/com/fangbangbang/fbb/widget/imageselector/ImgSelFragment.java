package com.fangbangbang.fbb.widget.imageselector;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.fangbangbang.fbb.widget.imageselector.adapter.FolderListAdapter;
import com.fangbangbang.fbb.widget.imageselector.adapter.ImageListAdapter;
import com.fangbangbang.fbb.widget.imageselector.adapter.PreviewAdapter;
import com.fangbangbang.fbb.widget.imageselector.bean.Folder;
import com.fangbangbang.fbb.widget.imageselector.bean.Image;
import com.fangbangbang.fbb.widget.imageselector.common.Callback;
import com.fangbangbang.fbb.widget.imageselector.common.OnFolderChangeListener;
import com.fangbangbang.fbb.widget.imageselector.common.OnItemClickListener;
import com.fangbangbang.fbb.widget.imageselector.utils.FileUtils;
import com.fangbangbang.fbb.widget.imageselector.utils.Platform;
import com.fangbangbang.fbb.widget.imageselector.widget.CustomViewPager;
import com.fangbangbang.fbb.widget.imageselector.widget.DividerGridItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImgSelFragment extends Fragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    private RecyclerView rvImageList;
    private Button btnAlbumSelected;
    private View rlBottom;
    private CustomViewPager viewPager;

    private ImgSelConfig config;
    private Callback callback;
    private List<Folder> folderList = new ArrayList<>();
    private List<Image> imageList = new ArrayList<>();
    private ArrayList<String> selectImagePathList = new ArrayList<>();

    private ListPopupWindow folderPopupWindow;
    private ImageListAdapter imageListAdapter;
    private FolderListAdapter folderListAdapter;
    private PreviewAdapter previewAdapter;

    private boolean hasFolderGened = false;

    private static final int LOADER_ALL = 0;
    private static final int REQUEST_CAMERA = 5;

    private static final int CAMERA_REQUEST_CODE = 1;

    private File tempFile;
    private Uri mCameraUri;

    public static ImgSelFragment instance() {
        ImgSelFragment fragment = new ImgSelFragment();
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.img_sel_fragment, container, false);
        rvImageList = (RecyclerView) view.findViewById(R.id.rvImageList);
        btnAlbumSelected = (Button) view.findViewById(R.id.btnAlbumSelected);
        btnAlbumSelected.setOnClickListener(this);
        rlBottom = view.findViewById(R.id.rlBottom);
        viewPager = (CustomViewPager) view.findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        config = ImageSelector.getImageConfig();
        selectImagePathList = config.originImagePath;
        try {
            callback = (Callback) getActivity();
        } catch (Exception e) {

        }

        btnAlbumSelected.setText(config.allImagesText);

        rvImageList.setLayoutManager(new GridLayoutManager(rvImageList.getContext(), 3));
        rvImageList.addItemDecoration(new DividerGridItemDecoration(rvImageList.getContext()));
        if (config.needCamera) {
            imageList.add(new Image());
        }
        imageListAdapter = new ImageListAdapter(getActivity(), imageList, config);
        imageListAdapter.setShowCamera(config.needCamera);
        imageListAdapter.setMultiSelect(config.multiSelect);
        rvImageList.setAdapter(imageListAdapter);
        imageListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public int onCheckedClick(int position, Image image) {
                return checkedImage(position, image);
            }

            @Override
            public void onImageClick(int position, Image image) {
                if (config.needCamera && position == 0) {
                    if (Platform.hasQ()) {
                        showCameraActionAndroidQ();
                    } else {
                        showCameraAction();
                    }
                } else {
                    if (config.multiSelect) {
                        viewPager.setAdapter((previewAdapter = new PreviewAdapter(getActivity(), imageList, config)));
                        previewAdapter.setListener(new OnItemClickListener() {
                            @Override
                            public int onCheckedClick(int position, Image image) {
                                return checkedImage(position, image);
                            }

                            @Override
                            public void onImageClick(int position, Image image) {
                                hidePreview();
                            }
                        });
                        if (config.needCamera) {
                            callback.onPreviewChanged(position, imageList.size() - 1, true);
                        } else {
                            callback.onPreviewChanged(position + 1, imageList.size(), true);
                        }
                        viewPager.setCurrentItem(config.needCamera ? position - 1 : position);
                        viewPager.setVisibility(View.VISIBLE);
                    } else {
                        if (callback != null) {
                            callback.onSingleImageSelected(image.path);
                        }
                    }
                }
            }
        });

        folderListAdapter = new FolderListAdapter(getActivity(), folderList, config);

        LoaderManager.getInstance(this).initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btnAlbumSelected.getId()) {
            if (folderPopupWindow == null) {
                WindowManager wm = getActivity().getWindowManager();
                int width = wm.getDefaultDisplay().getWidth();
                createPopupFolderList(width, width / 3 * 2);
            }

            if (folderPopupWindow.isShowing()) {
                folderPopupWindow.dismiss();
            } else {
                folderPopupWindow.show();
                if (folderPopupWindow.getListView() != null) {
                    folderPopupWindow.getListView().setDivider(new ColorDrawable(ContextCompat.getColor(getActivity(), R.color.bottom_bg)));
                }
                int index = folderListAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                folderPopupWindow.getListView().setSelection(index);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (Platform.hasQ()) {
                    if (mCameraUri != null) {
                        if (callback != null) {
                            callback.onCameraShot(mCameraUri.toString());
                        }
                    }
                } else {
                    if (tempFile != null) {
                        if (callback != null) {
                            callback.onCameraShot(tempFile);
                        }
                    }
                }
            } else {
                if (Platform.hasQ()) {
                    if (mCameraUri != null && !FileUtils.isAndroidQFileExists(getContext(), mCameraUri)) {
                        getContext().getContentResolver().delete(mCameraUri, null, null);
                    }
                } else {
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length >= 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Platform.hasQ()) {
                        showCameraActionAndroidQ();
                    } else {
                        showCameraAction();
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.permission_camera_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (config.needCamera) {
            callback.onPreviewChanged(position + 1, imageList.size() - 1, true);
        } else {
            callback.onPreviewChanged(position + 1, imageList.size(), true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private int checkedImage(int position, Image image) {
        if (image != null) {
            if (selectImagePathList.contains(image.path)) {
                selectImagePathList.remove(image.path);
                if (callback != null) {
                    callback.onImageUnselected(image.path);
                }
            } else {
                if (config.maxNum <= selectImagePathList.size()) {
                    Toast.makeText(getContext(), String.format(getString(R.string.maxnum), config.maxNum), Toast.LENGTH_SHORT).show();
                    return 0;
                }

                selectImagePathList.add(image.path);
                if (callback != null) {
                    callback.onImageSelected(image.path);
                }
            }
            return 1;
        }
        return 0;
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID};

        @TargetApi(Build.VERSION_CODES.Q)
        private final String[] IMAGE_PROJECTION_NEW = {
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (getActivity() != null) {
                if (id == LOADER_ALL) {
                    return new CursorLoader(getActivity(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            Platform.hasQ() ? IMAGE_PROJECTION_NEW : IMAGE_PROJECTION,
                            MediaStore.MediaColumns.SIZE + ">0", null, MediaStore.Images.Media.DATE_ADDED + " DESC");
                }
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                int count = data.getCount();
                if (count > 0) {
                    List<Image> tempImageList = new ArrayList<>();
                    data.moveToFirst();
                    do {
                        String name = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
                        String id = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                        String path;
                        String bucketName = "";
                        if (Platform.hasQ()) {
                            bucketName = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                            path = MediaStore.Images.Media
                                    .EXTERNAL_CONTENT_URI
                                    .buildUpon()
                                    .appendPath(String.valueOf(id)).build().toString();
                        } else {
                            path = data.getString(data.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        }
                        Image image = new Image(path, name, dateTime);
                        if (!name.endsWith("gif")) {
                            tempImageList.add(image);
                        }
                        if (!hasFolderGened) {
                            Folder folder;
                            if (Platform.hasQ()) {
                                folder = new Folder();
                                folder.name = bucketName;
                                folder.path = bucketName;
                            } else {
                                File imageFile = new File(path);
                                File folderFile = imageFile.getParentFile();
                                if (folderFile == null) {
                                    System.out.println(path);
                                    return;
                                }
                                folder = new Folder();
                                folder.name = folderFile.getName();
                                folder.path = folderFile.getAbsolutePath();
                            }
                            folder.cover = image;
                            if (!folderList.contains(folder)) {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                folder.images = imageList;
                                folderList.add(folder);
                            } else {
                                Folder f = folderList.get(folderList.indexOf(folder));
                                f.images.add(image);
                            }
                        }
                    } while (data.moveToNext());
                    imageList.clear();
                    if (config.needCamera) {
                        imageList.add(new Image());
                    }
                    imageList.addAll(tempImageList);
                    imageListAdapter.notifyDataSetChanged();
                    folderListAdapter.notifyDataSetChanged();
                    hasFolderGened = true;
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void createPopupFolderList(int width, int height) {
        folderPopupWindow = new ListPopupWindow(getActivity());
        folderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        folderPopupWindow.setAdapter(folderListAdapter);
        folderPopupWindow.setContentWidth(width);
        folderPopupWindow.setWidth(width);
        folderPopupWindow.setHeight(height);
        folderPopupWindow.setAnchorView(rlBottom);
        folderPopupWindow.setModal(true);
        folderListAdapter.setOnFloderChangeListener(new OnFolderChangeListener() {
            @Override
            public void onChange(int position, Folder folder) {
                folderPopupWindow.dismiss();
                if (position == 0) {
                    LoaderManager.getInstance(getActivity()).restartLoader(LOADER_ALL, null, mLoaderCallback);
                    btnAlbumSelected.setText(config.allImagesText);
                } else {
                    imageList.clear();
                    if (config.needCamera)
                        imageList.add(new Image());
                    imageList.addAll(folder.images);
                    imageListAdapter.notifyDataSetChanged();

                    btnAlbumSelected.setText(folder.name);
                }
            }
        });
    }

    public boolean hidePreview() {
        if (viewPager.getVisibility() == View.VISIBLE) {
            viewPager.setVisibility(View.GONE);
            callback.onPreviewChanged(0, 0, false);
            imageListAdapter.notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void showCameraActionAndroidQ() {
        if (config.maxNum <= selectImagePathList.size()) {
            Toast.makeText(getContext(), String.format(getString(R.string.maxnum), config.maxNum), Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return;
        }

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            mCameraUri = FileUtils.createImageUri(getContext(), getString(R.string.fbb));
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraUri);
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getContext(), getString(R.string.open_camera_failure), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCameraAction() {
        if (config.maxNum <= selectImagePathList.size()) {
            Toast.makeText(getContext(), String.format(getString(R.string.maxnum), config.maxNum), Toast.LENGTH_SHORT).show();
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            tempFile = new File(FileUtils.createRootPath(getActivity()) + "/" + System.currentTimeMillis() + ".jpg");
            FileUtils.createFile(tempFile);
            Uri uri = FileUtils.getUri(getActivity(), tempFile);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getContext(), getString(R.string.open_camera_failure), Toast.LENGTH_SHORT).show();
        }
    }

}
