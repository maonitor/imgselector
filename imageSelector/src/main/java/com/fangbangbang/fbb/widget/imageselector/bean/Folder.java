package com.fangbangbang.fbb.widget.imageselector.bean;

import java.util.List;

/**
 * Folder bean
 * Created by Yancy on 2015/12/2.
 */
public class Folder {

    public String name;
    public String path;
    public Image cover;
    public List<Image> images;

    public boolean isAll = false;

    public Folder() {

    }

    public Folder(boolean isAll) {
        this.isAll = isAll;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Folder) {
            Folder other = (Folder) obj;
            return (path != null && path.equals(other.path)
                    || (path == null && other.path == null));
        } else {
            return false;
        }
    }
}