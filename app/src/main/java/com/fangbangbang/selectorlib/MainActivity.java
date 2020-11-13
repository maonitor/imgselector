package com.fangbangbang.selectorlib;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fangbangbang.fbb.widget.imageselector.ImageSelector;
import com.fangbangbang.fbb.widget.imageselector.ImgSelActivity;
import com.fangbangbang.fbb.widget.imageselector.ImgSelConfig;
import com.fangbangbang.fbb.widget.imageselector.SelectImageLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvOpen = findViewById(R.id.tv_open);
        tvOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImageLoader loader = new SelectImageLoader() {
                    @Override
                    public void displayImage(Context context, String uri, ImageView imageView) {

                    }
                };
                ImgSelConfig config = new ImgSelConfig.Builder(MainActivity.this, loader)
                        .multiSelect(false)
                        .needCamera(true)
                        .needCrop(true)
                        .cropSize(1, 1, 200, 200)
                        .title("选择照片")
                        .build();
                ImageSelector.open(MainActivity.this, config, 1);
            }
        });
    }
}