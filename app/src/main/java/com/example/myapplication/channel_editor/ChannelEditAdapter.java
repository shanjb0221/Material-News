package com.example.myapplication.channel_editor;

/* REFERENCES
 * - [CSDN](https://blog.csdn.net/number_cmd9/article/details/119118035)
 *   [GITEE](https://gitee.com/jiugeishere/uidesign)
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
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
import com.example.myapplication.channel_pager.ChannelPagerAdapter;
import com.example.myapplication.databinding.ChannelHeaderMineBinding;
import com.example.myapplication.databinding.ChannelHeaderOtherBinding;
import com.example.myapplication.databinding.ChannelItemMineBinding;
import com.example.myapplication.databinding.ChannelItemOtherBinding;

import java.util.ArrayList;
import java.util.List;

public class ChannelEditAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    public static final int HEADER = 0;
    public static final int ITEM = 1;
    public static final int MINE = 0;
    public static final int OTHER = 2;
    public static final int ALL = 4;
    public static final int TYPE_HEADER_MINE = MINE | HEADER;
    public static final int TYPE_ITEM_MINE = MINE | ITEM;
    public static final int TYPE_HEADER_OTHER = OTHER | HEADER;
    public static final int TYPE_ITEM_OTHER = OTHER | ITEM;
    private static final long ANIM_DELAY = 300L;
    private static final long ANIM_TIME = 420L;
    private static final long DRAG_INTERVAL = 100L;

    private final LayoutInflater inflater;
    private final Handler delayHandler = new Handler();
    private final ItemTouchHelper itemTouchHelper;
    private final List<ChannelEntity> mine;
    private final List<ChannelEntity> other;
    private final RecyclerView parent;
    private final GridLayoutManager manager;
    private final int spanCount;
    private boolean isEditing = false;
    private BindingViewHolder<ChannelItemMineBinding> curViewHolder;
    private long startTime;
    private OnChannelItemClickListener itemClickListener;
    private OnDatasetChangedListener datasetChangedListener;
    private ChannelPagerAdapter channelPagerAdapter = null;

    public ChannelEditAdapter(Context context, RecyclerView parent, ItemTouchHelper helper, List<ChannelEntity> mine, List<ChannelEntity> other, GridLayoutManager manager) {
        this.inflater = LayoutInflater.from(context);
        this.parent = parent;
        this.itemTouchHelper = helper;
        this.mine = mine;
        this.other = other;
        this.manager = manager;
        this.spanCount = manager.getSpanCount();
    }

    @Override
    public int getItemViewType(int position) {
        return (position <= mine.size() ? MINE : OTHER) | (position == 0 || position == mine.size() + 1 ? HEADER : ITEM);
    }

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        assert parent == viewGroup;

        switch (viewType) {
            case TYPE_HEADER_MINE:
                BindingViewHolder<ChannelHeaderMineBinding> headerMineHolder = new BindingViewHolder<>(ChannelHeaderMineBinding.inflate(inflater, parent, false));
                headerMineHolder.B.editButton.setOnClickListener(view -> changeEditingMode());
                return headerMineHolder;
            case TYPE_HEADER_OTHER:
                return new BindingViewHolder<>(ChannelHeaderOtherBinding.inflate(inflater, parent, false));
            case TYPE_ITEM_MINE:
                BindingViewHolder<ChannelItemMineBinding> itemMineHolder = new BindingViewHolder<>(ChannelItemMineBinding.inflate(inflater, parent, false));
                itemMineHolder.setIsRecyclable(false);
                itemMineHolder.B.text.setOnClickListener(view -> {
                    if (isEditing)
                        moveItemMine(itemMineHolder);
                    else
                        itemClickListener.onItemClick(itemMineHolder.B.getRoot(), itemMineHolder.getAdapterPosition() - 1);
                });
                itemMineHolder.B.text.setOnLongClickListener(view -> {
                    if (!isEditing)
                        changeEditingMode();
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
                                if (startTime > 0 && System.currentTimeMillis() - startTime > DRAG_INTERVAL) {
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
                itemOtherHolder.B.text.setOnClickListener(v -> moveItemOther(itemOtherHolder));
                return itemOtherHolder;
        }

        assert false;
        return null;
    }

    private void moveItemMine(BindingViewHolder<ChannelItemMineBinding> holder) {
        int position = holder.getAdapterPosition();
        View srcView = manager.findViewByPosition(position);
        View dstView;
        float dstX, dstY;
        if (other.size() == 0) {                            // case 1: 其它频道为空
            dstView = manager.findViewByPosition(mine.size() + 1);
            assert dstView != null;
            dstX = dstView.getLeft();
            dstY = dstView.getBottom();
        } else {
            if ((mine.size() - 1) % spanCount == 0) {      // case 2: 我的频道最后一行仅有一个元素，移动后减少一行
                dstView = manager.findViewByPosition(mine.size() + 1);
            } else {                                        // case 3: default
                dstView = manager.findViewByPosition(mine.size() + 2);
            }
            assert dstView != null;
            dstX = dstView.getLeft();
            dstY = dstView.getTop();
        }
        // 如果 dstView 不在屏幕内,则 indexOfChild 为 -1, 此时不需要添加动画, 因为此时 notifyItemMoved 自带一个向目标移动的动画
        // 如果在屏幕内, 则添加一个位移动画
        if (parent.indexOfChild(dstView) != -1) {
            moveMineToOther(holder);
            startAnimation(srcView, dstX, dstY);
        } else {
            moveMineToOther(holder);
        }
    }

    private void moveItemOther(BindingViewHolder<ChannelItemOtherBinding> holder) {
        int position = holder.getAdapterPosition();
        View srcView = manager.findViewByPosition(position);
        View dstView;
        float dstX, dstY;
        if (mine.size() % spanCount == 0) {
            dstView = manager.findViewByPosition(mine.size() + 1);
            assert dstView != null;
            dstX = dstView.getLeft();
            dstY = dstView.getTop();
        } else {
            dstView = manager.findViewByPosition(mine.size());
            assert dstView != null;
            dstX = dstView.getRight();
            dstY = dstView.getTop();
        }

        // 如果在屏幕内, 则添加一个位移动画
        if (this.parent.indexOfChild(dstView) != -1) {
            // 如果当前位置是otherChannel可见的最后一个
            // 并且 当前位置不在grid的第一个位置
            // 并且 目标位置不在grid的第一个位置
            // 则 需要延迟250ms notifyItemMove , 这是因为这种情况 , 并不触发ItemAnimator , 会直接刷新界面
            // 导致我们的位移动画刚开始, 就已经 notify 完毕, 引起不同步问题
            if (position == manager.findLastVisibleItemPosition()
                    && (position - mine.size() - 2) % spanCount != 0
                    && mine.size() % spanCount != 0) {
                moveOtherToMineWithDelay(holder);
            } else {
                moveOtherToMine(holder);
            }
            startAnimation(srcView, dstX, dstY);
        } else {
            moveOtherToMine(holder);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        assert holder instanceof BindingViewHolder;
        BindingViewHolder h = (BindingViewHolder) holder;
        if (h.B instanceof ChannelHeaderMineBinding) {
            ChannelHeaderMineBinding b = (ChannelHeaderMineBinding) h.B;
            b.editButton.setText(isEditing ? "完成" : "编辑");
            b.tipsText.setText(isEditing ? "拖拽排序" : "长按编辑");
            return;
        }
        if (h.B instanceof ChannelItemMineBinding) {
            ChannelItemMineBinding b = (ChannelItemMineBinding) h.B;
            b.removeIcon.setVisibility(isEditing ? View.VISIBLE : View.GONE);
            b.text.setText(mine.get(position - 1).getName());
            shakeView(b.channelItemMine, isEditing);
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


    public List<ChannelEntity> getChannels(int category) {
        if (category == MINE) return mine;
        if (category == OTHER) return other;
        assert category == ALL;
        List<ChannelEntity> res = new ArrayList<>();
        res.addAll(mine);
        res.addAll(other);
        return res;
    }

    private void startAnimation(final View currentView, float dstX, float dstY) {
        ViewGroup viewGroup = (ViewGroup) parent.getParent();

        ImageView mirrorView = addMirrorView(viewGroup, currentView);

        Animation animation = getTranslateAnimator(
                dstX - currentView.getX(), dstY - currentView.getY());

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
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void moveMineToOther(BindingViewHolder<ChannelItemMineBinding> holder) {
        int position = holder.getAdapterPosition();
        int index = position - 1;
        assert 0 <= index && index < mine.size();
        ChannelEntity item = mine.get(index);
        mine.remove(index);
        other.add(0, item);
        notifyItemMoved(position, mine.size() + 2);
        if (channelPagerAdapter != null) channelPagerAdapter.notifyItemRemoved(index);
        if (datasetChangedListener != null) datasetChangedListener.onDatasetChanged();
    }

    private void moveOtherToMine(BindingViewHolder<ChannelItemOtherBinding> holder) {
        int position = processItemRemoveAdd(holder);
        notifyItemMoved(position, mine.size());
    }

    private void moveOtherToMineWithDelay(BindingViewHolder<ChannelItemOtherBinding> holder) {
        final int position = processItemRemoveAdd(holder);
        delayHandler.postDelayed(() -> notifyItemMoved(position, mine.size()), ANIM_DELAY);
    }

    private int processItemRemoveAdd(BindingViewHolder<ChannelItemOtherBinding> holder) {
        int position = holder.getAdapterPosition();

        int index = position - mine.size() - 2;
        assert 0 <= index && index < other.size();
        ChannelEntity item = other.get(index);
        other.remove(index);
        mine.add(item);
        if (channelPagerAdapter != null) channelPagerAdapter.notifyItemInserted(mine.size() - 1);
        if (datasetChangedListener != null) datasetChangedListener.onDatasetChanged();
        return position;
    }

    private ImageView addMirrorView(ViewGroup viewGroup, View view) {
        /*
          我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(parent.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        parent.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0] - parenLocations[0], locations[1] - parenLocations[1], 0, 0);
        viewGroup.addView(mirrorView, params);
        mirrorView.setElevation(100f);

        return mirrorView;
    }

    public void resetEditingMode() {
        if (isEditing) {
            isEditing = false;
            notifyItemRangeChanged(0, mine.size());
        }
    }

    private void changeEditingMode() {
        isEditing = !isEditing;
        /* NOTE
         * - notifyItemRangeChanged(0, mine.size()); doesn't work
         */
        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            TextView editTips = view.findViewById(R.id.tipsText);
            if (editTips != null) {
                editTips.setText(isEditing ? "拖拽排序" : "长按编辑");
            }
            Button editButton = view.findViewById(R.id.editButton);
            if (editButton != null) {
                editButton.setText(isEditing ? "完成" : "编辑");
            }
            ConstraintLayout itemView = view.findViewById(R.id.channelItemMine);
            if (itemView != null) {
                shakeView(itemView, isEditing);
            }
            ImageView imgEdit = view.findViewById(R.id.removeIcon);
            if (imgEdit != null) {
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

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        int fromIndex = fromPosition - 1, toIndex = toPosition - 1;
        ChannelEntity item = mine.get(fromIndex);
        mine.remove(fromIndex);
        mine.add(toIndex, item);
        notifyItemMoved(fromPosition, toPosition);
        if (channelPagerAdapter != null) channelPagerAdapter.notifyItemMoved(fromIndex, toIndex);
        if (datasetChangedListener != null) datasetChangedListener.onDatasetChanged();
    }

    @Override
    public void onChangeItem() {
        moveItemMine(curViewHolder);
    }

    public void setOnMyChannelItemClickListener(OnChannelItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setChannelPagerAdapter(ChannelPagerAdapter channelPagerAdapter) {
        this.channelPagerAdapter = channelPagerAdapter;
    }

    public void setOnDatasetChangedListener(OnDatasetChangedListener listener) {
        this.datasetChangedListener = listener;
    }

    public interface OnChannelItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnDatasetChangedListener {
        void onDatasetChanged();
    }

}
