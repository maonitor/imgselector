package com.fangbangbang.fbb.widget.imageselector.bean;

/**
 * Image bean
 * Created by Yancy on 2015/12/2.
 */
public class Image {

    public String path;
    public String name;
    public long time;

    public boolean isCamera = false;

    public Image(String path, String name, long time) {
        this.path = path;
        this.name = name;
        this.time = time;
    }

    public Image() {
        isCamera = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Image) {
            Image other = (Image) obj;
            return (path != null && path.equals(other.path)
                    || (path == null && other.path == null));
        } else {
            return false;
        }
    }
}