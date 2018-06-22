package com.ssig.smartcap.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.ssig.smartcap.R;
import com.ssig.smartcap.model.CaptureListItem;

public class AdapterCaptureList extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_ITEM = 1;

    private Context context;
    private List<CaptureListItem> captureListItems;
    private OnClickListener onClickListener;
    private OnCloseButtonClickListener onCloseButtonClickListener;

    private SparseBooleanArray selectedItems;
    private int currentSelectedIdx;

    public AdapterCaptureList(Context context, List<CaptureListItem> captureListItems) {
        this.context = context;
        this.selectedItems = new SparseBooleanArray();
        this.currentSelectedIdx = -1;
        this.onClickListener = null;
        this.onCloseButtonClickListener = null;
        this.captureListItems = captureListItems;
        this.processItems();
        this.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                processItems();
                super.onChanged();
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    // Holder STUFFS
    //----------------------------------------------------------------------------------------------
    class CaptureHolder extends RecyclerView.ViewHolder {

        private View layoutParent;

        private TextView textItemTitle;
        private TextView textItemDuration;
        private TextView textItemTimestamp;
        private ImageView imageSmartphoneEnable;
        private ImageView imageSmartwatchEnable;
        private TextView textItemDevices;

        private ImageView imageItemImageBackground;
        private ImageView imageItemImageIcon;

        private ImageButton buttonCloseCapture;

        CaptureHolder(View view) {
            super(view);

            this.layoutParent = view.findViewById(R.id.item_parent_layout);

            this.textItemTitle = view.findViewById(R.id.item_title_text);
            this.textItemDuration = view.findViewById(R.id.item_duration_text);
            this.textItemTimestamp = view.findViewById(R.id.item_timestamp_text);
            this.imageSmartphoneEnable = view.findViewById(R.id.item_smartphone_image);
            this.imageSmartwatchEnable = view.findViewById(R.id.item_smartwatch_image);
            this.textItemDevices = view.findViewById(R.id.item_devices_text);


            this.imageItemImageBackground = view.findViewById(R.id.item_image_background);
            this.imageItemImageIcon = view.findViewById(R.id.item_image_icon);
            this.buttonCloseCapture = view.findViewById(R.id.close_capture_button);

        }
    }

    public static class SectionViewHolder extends RecyclerView.ViewHolder {
        TextView textTitleSection;
        SectionViewHolder(View v) {
            super(v);
            textTitleSection = (TextView) v.findViewById(R.id.title_section_text);
        }
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == VIEW_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_captures_list_item, parent, false);
            holder = new CaptureHolder(itemView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_list_item_section, parent, false);
            holder = new SectionViewHolder(itemView);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        final CaptureListItem captureListItem = this.captureListItems.get(position);

        if (holder instanceof CaptureHolder) {

            CaptureHolder captureHolder = (CaptureHolder)holder;

            captureHolder.textItemTitle.setText(captureListItem.itemTitle);
            captureHolder.textItemTitle.setTextColor(context.getColor(captureListItem.closed ? R.color.colorPrimary : R.color.colorAlert));
            captureHolder.textItemDuration.setText(captureListItem.itemDuration);
            captureHolder.textItemDuration.setTextColor(context.getColor(captureListItem.closed ? R.color.colorPrimaryLight : R.color.colorAlert));

            captureHolder.textItemTimestamp.setText(captureListItem.itemTimestampText);

            captureHolder.imageSmartphoneEnable.setImageTintList(ColorStateList.valueOf(context.getColor(captureListItem.closed ? R.color.colorPrimaryLight : R.color.colorAlert)));
            captureHolder.imageSmartphoneEnable.setVisibility(captureListItem.itemSmartphoneEnable ? View.VISIBLE : View.GONE);

            captureHolder.imageSmartwatchEnable.setImageTintList(ColorStateList.valueOf(context.getColor(captureListItem.closed ? R.color.colorPrimaryLight : R.color.colorAlert)));
            captureHolder.imageSmartwatchEnable.setVisibility(captureListItem.itemSmartwatchEnable ? View.VISIBLE : View.GONE);

            captureHolder.textItemDevices.setText(captureListItem.itemDevices);

            captureHolder.layoutParent.setActivated(selectedItems.get(position, false));

            captureHolder.layoutParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onClickListener == null) return;
                    onClickListener.onItemClick(v, captureListItem, position);
                }
            });

            captureHolder.layoutParent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onClickListener == null) return false;
                    onClickListener.onItemLongClick(v, captureListItem, position);
                    return true;
                }
            });

            if (!captureListItem.closed) {
                captureHolder.buttonCloseCapture.setVisibility(View.VISIBLE);
                captureHolder.buttonCloseCapture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onCloseButtonClickListener == null) return;
                        onCloseButtonClickListener.onItemClick(v, captureListItem, position);
                    }
                });
            }else {
                captureHolder.buttonCloseCapture.setVisibility(View.GONE);
            }

            toggleCheckedIcon(captureHolder, position, captureListItem.closed);

        }else{
            SectionViewHolder view = (SectionViewHolder) holder;
            view.textTitleSection.setText(captureListItem.itemTitle);
        }

    }

    private void toggleCheckedIcon(CaptureHolder holder, int position, boolean closed) {
        if (selectedItems.get(position, false)) {
            holder.imageItemImageBackground.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.colorGreyMediumDark)));
            holder.imageItemImageIcon.setImageResource(R.drawable.ic_check);
            if (currentSelectedIdx == position) resetCurrentIndex();
        } else {
            int color = closed ? R.color.colorPrimaryLight : R.color.colorAlert;
            holder.imageItemImageBackground.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(this.context, color)));
            holder.imageItemImageIcon.setImageResource(closed ? R.drawable.ic_package_close : R.drawable.ic_package_open);
            if (currentSelectedIdx == position) resetCurrentIndex();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Item STUFFS
    //----------------------------------------------------------------------------------------------
    public CaptureListItem getItem(int position) {
        return this.captureListItems.get(position);
    }

    @Override
    public int getItemCount() {
        return this.captureListItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        int VIEW_SECTION = 0;
        return this.captureListItems.get(position).section ? VIEW_SECTION : VIEW_ITEM;
    }

    public void removeData(int position) {
        this.captureListItems.remove(position);
        resetCurrentIndex();
    }

    public void addItem(CaptureListItem captureListItem){
        this.captureListItems.add(captureListItem);
        resetCurrentIndex();
    }

    private void processItems(){

        boolean hasClosed = false;
        boolean hasUnclosed = false;

        Iterator<CaptureListItem> itemIterator = this.captureListItems.iterator();
        while(itemIterator.hasNext()){
            CaptureListItem item = itemIterator.next();
            if (item.section){
                itemIterator.remove();
            }else {
                hasClosed |= item.closed;
                hasUnclosed |= !item.closed;
            }
        }

        if (hasClosed){
            this.captureListItems.add(new CaptureListItem((true)));
        }

        if (hasUnclosed){
            this.captureListItems.add(new CaptureListItem((false)));
        }

        Collections.sort(this.captureListItems, new Comparator<CaptureListItem>() {
            @Override
            public int compare(CaptureListItem item1, CaptureListItem item2) {
                Boolean closed1 = item1.closed;
                Boolean closed2 = item2.closed;
                int comp = closed1.compareTo(closed2);
                if (comp != 0)
                    return comp;

                Boolean section1 = item1.section;
                Boolean section2 = item2.section;
                comp = section1.compareTo(section2);
                if (comp != 0)
                    return -comp;

                return item1.itemTimestampLong.compareTo(item2.itemTimestampLong);
            }
        });
    }

    private void resetCurrentIndex() {
        currentSelectedIdx = -1;
    }

    public void replaceItemsList(List<CaptureListItem> captureListItems){
        this.captureListItems.clear();
        this.captureListItems.addAll(captureListItems);
    }

    //----------------------------------------------------------------------------------------------
    // Selection STUFFS
    //----------------------------------------------------------------------------------------------
    public void toggleSelection(int pos) {
        currentSelectedIdx = pos;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    //----------------------------------------------------------------------------------------------
    // Listeners STUFFS
    //----------------------------------------------------------------------------------------------
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnCloseButtonClickListener(OnCloseButtonClickListener onCloseButtonClickListener) {
        this.onCloseButtonClickListener = onCloseButtonClickListener;
    }

    public interface OnClickListener {
        void onItemClick(View view, CaptureListItem obj, int pos);
        void onItemLongClick(View view, CaptureListItem obj, int pos);
    }

    public interface OnCloseButtonClickListener {
        void onItemClick(View view, CaptureListItem obj, int pos);
    }

}
