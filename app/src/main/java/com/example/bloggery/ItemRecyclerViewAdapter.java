package com.example.bloggery;


import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.bloggery.model.Item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ItemRecyclerViewAdapter extends RecyclerView.Adapter<ItemRecyclerViewAdapter.ViewHolder> {

    private final List<Item> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public ItemRecyclerViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }

    public void addItems(List<Item> data) {
        mData.addAll(data);
        notifyDataSetChanged();
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Item item = mData.get(position);
        holder.mTitle.setText(item.getTitle());
        holder.mPostedBy.setText(Html.fromHtml(String.format("<b>Post by :</b> %s", item.getAuthor())));
        Picasso.get().load(item.getPhotoUrl()).placeholder(R.drawable.profile_place_holder).into(holder.mImageItem);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTitle;
        TextView mPostedBy;
        ImageView mImageItem;

        ViewHolder(View itemView) {
            super(itemView);
            mTitle = itemView.findViewById(R.id.item_title);
            mPostedBy = itemView.findViewById(R.id.item_post_by);
            mImageItem = itemView.findViewById(R.id.item_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, mData.get(getAdapterPosition()));
        }
    }

    // convenience method for getting data at click position
    Item getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, Item position);
    }
}
