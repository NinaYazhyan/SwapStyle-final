package com.example.signuploginrealtime;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.ViewHolder> {

    private Context context;
    private List<WardrobeItem> itemList;

    public WardrobeAdapter(Context context, List<WardrobeItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, toggleIcon;
        TextView titleTextView, descriptionTextView;
        TextView categoryTextView, sizeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.wardrobeImage);
            titleTextView = itemView.findViewById(R.id.wardrobeTitle);
            descriptionTextView = itemView.findViewById(R.id.wardrobeDescription);
            toggleIcon = itemView.findViewById(R.id.toggleIcon);
            categoryTextView = itemView.findViewById(R.id.wardrobeCategory);
            sizeTextView = itemView.findViewById(R.id.wardrobeSize);
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

        holder.titleTextView.setText(item.getTitle());
        holder.descriptionTextView.setText(item.getDescription());
        holder.categoryTextView.setText(item.getCategory() + " - " + item.getSubcategory());
        holder.sizeTextView.setText("Size: " + item.getSize());

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
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}