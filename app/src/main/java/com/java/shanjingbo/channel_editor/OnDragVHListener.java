package com.java.shanjingbo.channel_editor;

/**
 * ViewHolder 被选中 以及 拖拽释放 触发监听器
 * tangxianfeng 2021.7.19.
 */
public interface OnDragVHListener {
    /**
     * Item被选中时触发
     */
    void onItemSelected();


    /**
     * Item在拖拽结束/滑动结束后触发
     */
    void onItemFinish();
}
