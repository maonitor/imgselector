package com.fangbangbang.fbb.widget.imageselector;

import java.util.List;

/**
 * 作者：Rimon on 2017/5/28 19:47
 */
public interface DataHelper<T> {

    boolean addAll(List<T> list);

    boolean addAll(int position, List<T> list);

    void add(T data);

    void add(int position, T data);

    void clear();

    boolean contains(T data);

    T getData(int index);

    void modify(T oldData, T newData);

    void modify(int index, T newData);

    boolean remove(T data);

    void remove(int index);

}
