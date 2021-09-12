package com.java.shanjingbo.channel_editor;

/* REFERENCES
 * - [CSDN](https://blog.csdn.net/number_cmd9/article/details/119118035)
 *   [GITEE](https://gitee.com/jiugeishere/uidesign)
 */

public interface OnItemMoveListener {
    void onItemMove(int fromPosition, int toPosition);

    void onChangeItem();
}
