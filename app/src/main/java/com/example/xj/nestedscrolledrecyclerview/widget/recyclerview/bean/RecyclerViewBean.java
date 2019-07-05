package com.example.xj.nestedscrolledrecyclerview.widget.recyclerview.bean;

import java.io.Serializable;

public class RecyclerViewBean implements Serializable {
    public static final int TYPE_TOP_ITEM = 1;//图片
    public static final int TYPE_BOTTOM_ITEM = 2;//标题
    private String id;
    private int type;
    private Object data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
