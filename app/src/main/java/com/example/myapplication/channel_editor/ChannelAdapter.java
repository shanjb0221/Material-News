package com.example.myapplication.channel_editor;

/* REFERENCES
 * - [CSDN](https://blog.csdn.net/number_cmd9/article/details/119118035)
 *   [GITEE](https://gitee.com/jiugeishere/uidesign)
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.BindingViewHolder;
import com.example.myapplication.databinding.ChannelHeaderMineBinding;
import com.example.myapplication.databinding.ChannelHeaderOtherBinding;
import com.example.myapplication.databinding.ChannelItemMineBinding;
import com.example.myapplication.databinding.ChannelItemOtherBinding;

import java.util.ArrayList;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    public static final int HEADER = 0;
    public static final int ITEM = 1;
    public static final int MINE = 0;
    public static final int OTHER = 2;
    public static final int ALL = 4;
    public static final int TYPE_HEADER_MINE = MINE | HEADER;
    public static final int TYPE_ITEM_MINE = MINE | ITEM;
    public static final int TYPE_HEADER_OTHER = OTHER | HEADER;
    public static final int TYPE_ITEM_OTHER = OTHER | ITEM;
    private static final long ANIM_TIME = 360L;
    private static final long TIME_INTERVAL = 100;
    private final Context context;
    private final LayoutInflater inflater;
    private final Handler delayHandler = new Handler();
    private final ItemTouchHelper itemTouchHelper;
    private final List<ChannelEntity> mine;
    private final List<ChannelEntity> other;
    private boolean isEditing;
    private OnChannelItemClickListener itemClickListener;
    private BindingViewHolder<ChannelItemMineBinding> curViewHolder;
    private long startTime;
    private RecyclerView pView;
    private GridLayoutManager manager;

    public ChannelAdapter(Context context, ItemTouchHelper helper, List<ChannelEntity> mine, List<ChannelEntity> other) {
        this.context = context;
        this.itemTouchHelper = helper;
        this.inflater = LayoutInflater.from(context);
        this.mine = mine;
        this.other = other;
    }

    @Override
    public int getItemViewType(int position) {
        return (position <= mine.size() ? MINE : OTHER) | (position == 0 || position == mine.size() + 1 ? HEADER : ITEM);
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        assert parent instanceof RecyclerView;
        this.pView = (RecyclerView) parent;
        assert pView.getLayoutManager() instanceof GridLayoutManager;
        this.manager = (GridLayoutManager) pView.getLayoutManager();

        switch (viewType) {
            case TYPE_HEADER_MINE:
                BindingViewHolder<ChannelHeaderMineBinding> headerMineHolder = new BindingViewHolder<>(ChannelHeaderMineBinding.inflate(inflater, parent, false));
                headerMineHolder.B.editButton.setOnClickListener(view -> {
                    isEditing = !isEditing;
                    changeEditingMode(pView);
                    assert view instanceof Button;
                    ((Button) view).setText(isEditing ? "完成" : "编辑");
                });
                return headerMineHolder;
            case TYPE_HEADER_OTHER:
                return new BindingViewHolder<>(ChannelHeaderOtherBinding.inflate(inflater, parent, false));
            case TYPE_ITEM_MINE:
                BindingViewHolder<ChannelItemMineBinding> itemMineHolder = new BindingViewHolder<>(ChannelItemMineBinding.inflate(inflater, parent, false));
                itemMineHolder.setIsRecyclable(false);
                itemMineHolder.B.text.setOnClickListener(view -> {
                    int position = itemMineHolder.getAdapterPosition();
                    if (isEditing) {
                        int spanCount = manager.getSpanCount();
                        View srcView = manager.findViewByPosition(position);
                        // 移动后高度变化 (我的频道Grid 最后一个 item 在新的一行第一个)
                        View dstView = manager.findViewByPosition(mine.size() + ((mine.size() - 1) % spanCount == 0 ? 1 : 2));


                        // 如果 dstView 不在屏幕内,则 indexOfChild 为 -1, 此时不需要添加动画, 因为此时 notifyItemMoved 自带一个向目标移动的动画
                        // 如果在屏幕内, 则添加一个位移动画
                        // **注意!** 当其它频道为空时, dstView 为 null
                        if (dstView != null && pView.indexOfChild(dstView) != -1) {
                            moveMineToOther(itemMineHolder);
                            startAnimation(pView, srcView, dstView.getLeft(), dstView.getTop(), true);
                        } else {
                            moveMineToOther(itemMineHolder);
                        }
                    } else {
                        itemClickListener.onItemClick(itemMineHolder.B.getRoot(), position - 1);
                    }
                });
                itemMineHolder.B.text.setOnLongClickListener(view -> {
                    if (!isEditing) {
                        isEditing = true;
                        changeEditingMode(pView);
                        View button = pView.getChildAt(0).findViewById(R.id.editButton);
                        assert button instanceof Button;
                        ((Button) button).setText("完成");
                    }
                    itemTouchHelper.startDrag(itemMineHolder);
                    return true;
                });
                itemMineHolder.B.text.setOnTouchListener((view, motionEvent) -> {
                    if (isEditing) {
                        curViewHolder = itemMineHolder;
                        switch (motionEvent.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                startTime = System.currentTimeMillis();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (startTime > 0 && System.currentTimeMillis() - startTime > TIME_INTERVAL) {
                                    itemTouchHelper.startDrag(itemMineHolder);
                                }
                                break;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_UP:
                                startTime = -1;
                                break;
                        }
                    }
                    return false;
                });
                shakeView(itemMineHolder.itemView, isEditing);
                return itemMineHolder;
            case TYPE_ITEM_OTHER:
                BindingViewHolder<ChannelItemOtherBinding> itemOtherHolder = new BindingViewHolder<>(ChannelItemOtherBinding.inflate(inflater, parent, false));
                itemOtherHolder.B.text.setOnClickListener(v -> {
                    int position = itemOtherHolder.getAdapterPosition();
                    // 如果 RecyclerView 滑动到底部, 移动的目标位置的y轴 - height
                    View srcView = manager.findViewByPosition(position);
                    // 目标位置的前一个item  即当前MyChannel的最后一个
                    View preDstView = manager.findViewByPosition(mine.size());

                    // 如果targetView不在屏幕内,则为 -1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
                    // 如果在屏幕内,则添加一个位移动画
                    if (pView.indexOfChild(preDstView) != -1) {
                        int targetX = preDstView.getLeft();
                        int targetY = preDstView.getTop();

                        int targetPosition = mine.size() + 1;

                        int spanCount = manager.getSpanCount();
                        // target 在最后一行第一个
                        if ((targetPosition - 1) % spanCount == 0) {
                            View targetView = manager.findViewByPosition(targetPosition);
                            targetX = targetView.getLeft();
                            targetY = targetView.getTop();
                        } else {
                            targetX += preDstView.getWidth();

                            // 最后一个item可见
                            if (manager.findLastVisibleItemPosition() == getItemCount() - 1) {
                                // 最后的item在最后一行第一个位置
                                if ((getItemCount() - 1 - mine.size() - 2) % spanCount == 0) {
                                    // RecyclerView实际高度 > 屏幕高度 && RecyclerView实际高度 < 屏幕高度 + item.height
                                    int firstVisiblePostion = manager.findFirstVisibleItemPosition();
                                    if (firstVisiblePostion == 0) {
                                        // FirstCompletelyVisibleItemPosition == 0 即 内容不满一屏幕 , targetY值不需要变化
                                        // // FirstCompletelyVisibleItemPosition != 0 即 内容满一屏幕 并且 可滑动 , targetY值 + firstItem.getTop
                                        if (manager.findFirstCompletelyVisibleItemPosition() != 0) {
                                            int offset = (-pView.getChildAt(0).getTop()) - pView.getPaddingTop();
                                            targetY += offset;
                                        }
                                    } else { // 在这种情况下 并且 RecyclerView高度变化时(即可见第一个item的 position != 0),
                                        // 移动后, targetY值  + 一个item的高度
                                        targetY += preDstView.getHeight();
                                    }
                                }
                            } else {
                            }
                        }

                        // 如果当前位置是otherChannel可见的最后一个
                        // 并且 当前位置不在grid的第一个位置
                        // 并且 目标位置不在grid的第一个位置

                        // 则 需要延迟250秒 notifyItemMove , 这是因为这种情况 , 并不触发ItemAnimator , 会直接刷新界面
                        // 导致我们的位移动画刚开始,就已经notify完毕,引起不同步问题
                        if (position == manager.findLastVisibleItemPosition()
                                && (position - mine.size() - 2) % spanCount != 0
                                && (targetPosition - 1) % spanCount != 0) {
                            moveOtherToMineWithDelay(itemOtherHolder);
                        } else {
                            moveOtherToMine(itemOtherHolder);
                        }
                        startAnimation(pView, srcView, targetX, targetY, false);
                    } else {
                        moveOtherToMine(itemOtherHolder);
                    }
                });
                return itemOtherHolder;

        }
        assert false;
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        assert holder instanceof BindingViewHolder;
        BindingViewHolder h = (BindingViewHolder) holder;
        if (h.B instanceof ChannelHeaderMineBinding) {
            ChannelHeaderMineBinding b = (ChannelHeaderMineBinding) h.B;
            b.editButton.setText(isEditing ? "完成" : "编辑");
            return;
        }
        if (h.B instanceof ChannelItemMineBinding) {
            ChannelItemMineBinding b = (ChannelItemMineBinding) h.B;
            b.removeIcon.setVisibility(isEditing ? View.VISIBLE : View.GONE);
            b.text.setText(mine.get(position - 1).getName());
            return;
        }
        if (h.B instanceof ChannelHeaderOtherBinding) {
            return;
        }
        if (h.B instanceof ChannelItemOtherBinding) {
            ChannelItemOtherBinding b = (ChannelItemOtherBinding) h.B;
            b.text.setText(other.get(position - mine.size() - 2).getName());
        }
    }

    @Override
    public int getItemCount() {
        return mine.size() + other.size() + 2;
    }

    public void reset() {
        if (isEditing) {
            isEditing = false;
            notifyItemRangeChanged(0, mine.size());
        }
    }

    public List<ChannelEntity> getChannels(int category) {
        if (category == MINE) return mine;
        if (category == OTHER) return other;
        assert category == ALL;
        List<ChannelEntity> res = new ArrayList<>();
        res.addAll(mine);
        res.addAll(other);
        return res;
    }

    private void startAnimation(RecyclerView recyclerView, final View currentView, float dstX, float dstY, boolean isFromMine) {
        ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();

        ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);
        assert mirrorView != null;

        Animation animation = getTranslateAnimator(
                dstX - currentView.getLeft(), dstY - currentView.getTop());
        assert animation != null;

        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewGroup.removeView(mirrorView);
                if (currentView.getVisibility() == View.INVISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                    //ShakeView(currentView,isEditMode&&isfromme);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void moveMineToOther(BindingViewHolder<ChannelItemMineBinding> holder) {
        int position = holder.getAdapterPosition();

        int startPosition = position - 1;
        if (startPosition > mine.size() - 1) {
            return;
        }
        ChannelEntity item = mine.get(startPosition);
        mine.remove(startPosition);
        other.add(0, item);
        notifyItemMoved(position, mine.size() + 2);
    }

    private void moveOtherToMine(BindingViewHolder<ChannelItemOtherBinding> holder) {
        int position = processItemRemoveAdd(holder);
        if (position == -1) return;
        notifyItemMoved(position, mine.size());
    }

    private void moveOtherToMineWithDelay(BindingViewHolder<ChannelItemOtherBinding> holder) {
        final int position = processItemRemoveAdd(holder);
        if (position == -1)
            return;
        delayHandler.postDelayed(() -> notifyItemMoved(position, mine.size()), ANIM_TIME);
    }

    private int processItemRemoveAdd(BindingViewHolder<ChannelItemOtherBinding> holder) {
        int position = holder.getAdapterPosition();

        int startPosition = position - mine.size() - 2;
        assert startPosition <= other.size() - 1;
        ChannelEntity item = other.get(startPosition);
        other.remove(startPosition);
        mine.add(item);
        return position;
    }

    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(recyclerView.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        recyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);

        return mirrorView;
    }

    private void changeEditingMode(RecyclerView parent) {
        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView imgEdit = view.findViewById(R.id.removeIcon);
            ConstraintLayout itemView = view.findViewById(R.id.channelItemMine);
            TextView editTips = view.findViewById(R.id.tipsText);
            if (editTips != null) {
                editTips.setVisibility(isEditing ? View.VISIBLE : View.GONE);
            }
            if (itemView != null) {
                Log.e("ChannelTest", "1111" + i);
                shakeView(itemView, isEditing);
            }
            if (imgEdit != null) {
                Log.e("ChannelTest", "2222" + i);
                imgEdit.setVisibility(isEditing ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void shakeView(View view, boolean shake) {
        if (shake) {
            if (view.getAnimation() != null) {
                view.getAnimation().start();
            }
            RotateAnimation rotate = new RotateAnimation(-1, 1, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
            LinearInterpolator lin = new LinearInterpolator();
            rotate.setInterpolator(lin);
            rotate.setDuration(100);
            rotate.setRepeatCount(-1);
            rotate.setRepeatMode(Animation.REVERSE);
            rotate.setFillAfter(false);
            rotate.setStartOffset(10);
            view.startAnimation(rotate);
        } else {
            if (view.getAnimation() != null) {
                view.getAnimation().cancel();
            }
            view.clearAnimation();
        }

    }

    private TranslateAnimation getTranslateAnimator(float dX, float dY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, dX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, dY);
        // RecyclerView 默认移动动画 250ms 这里设置 360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }

    public void setOnMyChannelItemClickListener(OnChannelItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ChannelEntity item = mine.get(fromPosition - 1);
        mine.remove(fromPosition - 1);
        mine.add(toPosition - 1, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChangeItem() {
        int position = curViewHolder.getAdapterPosition();
        View targetView = manager.findViewByPosition(mine.size() + 2);
        View currentView = manager.findViewByPosition(position);
        // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
        // 如果在屏幕内,则添加一个位移动画
        if (pView.indexOfChild(targetView) >= 0) {
            int targetX, targetY;
            int spanCount = manager.getSpanCount();

            // 移动后 高度将变化 (我的频道Grid 最后一个item在新的一行第一个)
            if ((mine.size() - 1) % spanCount == 0) {
                View preTargetView = manager.findViewByPosition(mine.size() + 2 - 1);
                targetX = preTargetView.getLeft();
                targetY = preTargetView.getTop();
            } else {
                targetX = targetView.getLeft();
                targetY = targetView.getTop();
            }

            moveMineToOther(curViewHolder);
            startAnimation(pView, currentView, targetX, targetY, false);

        } else {
            moveMineToOther(curViewHolder);
        }

    }

    public interface OnChannelItemClickListener {
        void onItemClick(View view, int position);
    }

}
