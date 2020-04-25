package com.hifnawy.decideForMe;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hifnawy.spinningWheelLib.model.WheelSection;
import com.hifnawy.spinningWheelLib.model.WheelTextSection;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class CustomListViewAdapter extends ArrayAdapter<WheelSection> {

    private Context mContext;
    private ListViewAdapterSizeChangedListner mSizeChangedListener;
    private List<WheelSection> dataSet;
    private int lastPosition = -1;

    public CustomListViewAdapter(Context context, ArrayList<WheelSection> data) {
        super(context, R.layout.list_item, data);
        this.dataSet = data;
        this.mContext = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        WheelSection dataItem = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.optionTxtName = (TextView) convertView.findViewById(R.id.optionText);
            viewHolder.optionTxtContainer = (LinearLayout) convertView.findViewById(R.id.optionTxtContainer);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        switch (dataItem.getType()) {
            case TEXT:
                viewHolder.optionTxtName.setText(((WheelTextSection) dataItem).getText());
                viewHolder.optionTxtName.setTextColor(((WheelTextSection) dataItem).getForegroundColor());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    viewHolder.optionTxtContainer.setBackgroundTintList(ColorStateList.valueOf(((WheelTextSection) dataItem).getBackgroundColor()));
                }
                /*viewHolder.optionTxtContainer.setOnClickListener(this);*/
                viewHolder.optionTxtContainer.setTag(position);
        }

        // Return the completed view to render on screen
        return convertView;
    }

    @Override
    public void add(@Nullable WheelSection object) {
        super.add(object);

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    @Override
    public void clear() {
        super.clear();

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    public void updateItem(int index, WheelTextSection ws) {
        dataSet.set(index, ws);
        notifyDataSetChanged();
    }

    public void remove(int index) {
        dataSet.remove(index);
        notifyDataSetChanged();

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    public void setItems(List<WheelSection> newWheelSections) {
        this.dataSet = newWheelSections;
        notifyDataSetChanged();

        if (mSizeChangedListener != null) {
            mSizeChangedListener.onSizeChanged(this.dataSet.size());
        }
    }

    public List<WheelSection> getItems() {
        return dataSet;
    }

    public void setSizeChangedListener(ListViewAdapterSizeChangedListner mSizeChangedListener) {
        this.mSizeChangedListener = mSizeChangedListener;
    }

    // View lookup cache
    private static class ViewHolder {
        LinearLayout optionTxtContainer;
        TextView optionTxtName;
    }
}