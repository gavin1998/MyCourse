package com.example.cfy.mycourse;

/**
 * Created by cfy on 2015/2/11.
 */
//一个课程对应的数据：mId对应于格子（grid)的序列号，mContent对应课程名称
public class Schedule {
    private int mId;
    private String mContent;

    public Schedule(int id, String content) {
        mId = id;
        mContent = content;
    }
    public int getId() {
        return mId;
    }
    public void setId(int id) {
        mId = id;
    }
    public String getContent() {
        return mContent;
    }
    public void setContent(String content) {
        mContent = content;
    }

}