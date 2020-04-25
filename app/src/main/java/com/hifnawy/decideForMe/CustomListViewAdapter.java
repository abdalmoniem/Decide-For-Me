package com.hifnawy.decideForMe;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hifnawy.spinningWheelLib.model.WheelSection;
import com.hifnawy.spinningWheelLib.model.WheelTextSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class CustomListViewAdapter extends RecyclerView.Adapter<CustomListViewAdapter.ViewHolder> {

    private Context mContext;
    private List<WheelSection> dataSet;
    private RecyclerViewItemClickListner mClickListener;
    private ListViewAdapterSizeChangedListner mSizeChangedListener;

    private RecyclerView recyclerView;

    public CustomListViewAdapter(Context context) {
        // super(context, R.layout.list_item, data);
        this.dataSet = new ArrayList<>();
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final WheelSection dataItem = this.dataSet.get(position);

        switch (dataItem.getType()) {
            case TEXT:
                holder.optionTxtName.setText(((WheelTextSection) dataItem).getText());
                holder.optionTxtName.setTextColor(((WheelTextSection) dataItem).getForegroundColor());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.optionTxtContainer.setBackgroundTintList(ColorStateList.valueOf(((WheelTextSection) dataItem).getBackgroundColor()));
                }
                holder.optionTxtContainer.setTag(position);

                if (mClickListener != null) {
                    holder.optionTxtContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mClickListener.onItemClicked(position);
                        }
                    });

                    holder.optionTxtContainer.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            mClickListener.onItemLongClicked(position);
                            return true;
                        }
                    });
                }

                break;
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    public void setItemClickListener(RecyclerViewItemClickListner mClickListener) {
        this.mClickListener = mClickListener;
    }

    public void setItems(List<WheelSection> newWheelSections) {
        this.dataSet = newWheelSections;
        notifyDataSetChanged();

        if (recyclerView != null) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_animation_fall_down));

            recyclerView.scrollToPosition(this.dataSet.size() - 1);
        }

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    public void add(@Nullable WheelSection object) {
        // super.add(object);
        this.dataSet.add(object);
        notifyDataSetChanged();

        if (recyclerView != null) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_animation_rise_up));

            recyclerView.scrollToPosition(this.dataSet.size() - 1);
        }

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    public WheelSection getItem(int position) {
        return this.dataSet.get(position);
    }

    public List<WheelSection> getItems() {
        return dataSet;
    }

    public void updateItem(int index, WheelTextSection ws) {
        dataSet.set(index, ws);
        notifyDataSetChanged();

        if (recyclerView != null) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_animation_fall_down));

            recyclerView.scrollToPosition(this.dataSet.size() - 1);
        }

        if (recyclerView != null) {
            recyclerView.scheduleLayoutAnimation();
        }
    }

    public void shuffle() {
        for (int i = 0; i < getItemCount(); i++) {
            WheelTextSection item = (WheelTextSection) getItem(i);

            int bR = new Random().nextInt(255);
            int bG = new Random().nextInt(255);
            int bB = new Random().nextInt(255);

            int fR = ~bR;
            int fG = ~bG;
            int fB = ~bB;

            updateItem(i, new WheelTextSection(item.getText())
                    .setSectionForegroundColor(Color.rgb(fR, fG, fB))
                    .setSectionBackgroundColor(Color.rgb(bR, bG, bB)));
        }
    }

    public void remove(int index) {
        dataSet.remove(index);
        notifyDataSetChanged();

        if (recyclerView != null) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_animation_fall_down));

            recyclerView.scrollToPosition(this.dataSet.size() - 1);
        }

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    public void clear() {
        // super.clear();
        this.dataSet.clear();
        notifyDataSetChanged();

        if (recyclerView != null) {
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(mContext, R.anim.layout_animation_fall_down));

            recyclerView.scrollToPosition(this.dataSet.size() - 1);
        }

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    @Override
    public int getItemCount() {
        return this.dataSet.size();
    }

    public void setSizeChangedListener(ListViewAdapterSizeChangedListner mSizeChangedListener) {
        this.mSizeChangedListener = mSizeChangedListener;
    }

    // View lookup cache
    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout optionTxtContainer;
        TextView optionTxtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            optionTxtContainer = itemView.findViewById(R.id.optionTxtContainer);
            optionTxtName = itemView.findViewById(R.id.optionText);
        }
    }
}