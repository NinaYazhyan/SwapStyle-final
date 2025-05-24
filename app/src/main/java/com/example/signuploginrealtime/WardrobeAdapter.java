package com.example.signuploginrealtime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.ViewHolder> {

    private Context context;
    private List<WardrobeItem> itemList;
    private OnMessageClickListener messageListener;

    public interface OnMessageClickListener {
        void onMessageClick(WardrobeItem item);
    }

    public WardrobeAdapter(Context context, List<WardrobeItem> itemList, OnMessageClickListener listener) {
        this.context = context;
        this.itemList = itemList;
        this.messageListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, toggleIcon;
        TextView titleTextView, descriptionTextView, userNameTextView;
        TextView categoryTextView, sizeTextView;
        Button messageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.wardrobeImage);
            titleTextView = itemView.findViewById(R.id.wardrobeTitle);
            descriptionTextView = itemView.findViewById(R.id.wardrobeDescription);
            userNameTextView = itemView.findViewById(R.id.wardrobeUserName);
            toggleIcon = itemView.findViewById(R.id.toggleIcon);
            categoryTextView = itemView.findViewById(R.id.wardrobeCategory);
            sizeTextView = itemView.findViewById(R.id.wardrobeSize);
            messageButton = itemView.findViewById(R.id.buttonMessage);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wardrobe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WardrobeItem item = itemList.get(position);

        // Load image with Glide and make it larger
        Glide.with(context)
                .load(item.getImageUrl())
                .override(800, 800) // Set larger dimensions
                .centerCrop()
                .into(holder.imageView);

        // Add image click listener to show dialog
        holder.imageView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                Fragment currentFragment = ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .findFragmentById(R.id.frame_lay);

                if (currentFragment instanceof HomeFragment) {
                    ((HomeFragment) currentFragment).showImageDialog(item.getImageUrl());
                }
            }
        });

        holder.messageButton.setOnClickListener(v -> {
            if (messageListener != null) {
                messageListener.onMessageClick(item);
            }
        });
        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        holder.categoryTextView.setText(item.getCategory() + " - " + item.getSubcategory());
        holder.sizeTextView.setText("Size: " + item.getSize());

        // Set user name
        if (item.getUserName() != null && !item.getUserName().isEmpty()) {
            holder.userNameTextView.setText("Owner: " + item.getUserName());
        } else {
            holder.userNameTextView.setText("Owner: Test User");
        }

        // Initially hide the description
        holder.descriptionTextView.setVisibility(View.GONE);
        holder.toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24);

        // Toggle description visibility
        holder.toggleIcon.setOnClickListener(v -> {
            if (holder.descriptionTextView.getVisibility() == View.VISIBLE) {
                holder.descriptionTextView.setVisibility(View.GONE);
                holder.toggleIcon.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                holder.descriptionTextView.setVisibility(View.VISIBLE);
                holder.toggleIcon.setImageResource(R.drawable.baseline_visibility_24);
            }
        });

        // Set message button click listener
        holder.messageButton.setOnClickListener(v -> {
            if (messageListener != null) {
                messageListener.onMessageClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateList(List<WardrobeItem> newList) {
        itemList.clear();
        itemList.addAll(newList);
        notifyDataSetChanged();
    }
}