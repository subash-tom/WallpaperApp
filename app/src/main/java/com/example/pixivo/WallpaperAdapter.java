package com.example.pixivo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    private final Activity context;
    private final List<WallpaperModel> list = new ArrayList<>();
    private final List<WallpaperModel> fullList = new ArrayList<>();

    public WallpaperAdapter(Activity context, List<WallpaperModel> data) {
        this.context = context;

        if (data != null) {
            list.addAll(data);
            fullList.addAll(data);
        }
    }

    public void updateFullList(List<WallpaperModel> data) {
        list.clear();
        fullList.clear();

        list.addAll(data);
        fullList.addAll(data);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wallpaper_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        WallpaperModel wallpaper = list.get(position);

        holder.loadingGif.setVisibility(View.VISIBLE);

        Glide.with(context)
                .asGif()
                .load(R.drawable.loading)
                .into(holder.loadingGif);

        Glide.with(context)
                .load(wallpaper.getImageUrl())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(
                            GlideException e,
                            Object model,
                            Target<Drawable> target,
                            boolean isFirstResource) {

                        holder.loadingGif.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource,
                            Object model,
                            Target<Drawable> target,
                            DataSource dataSource,
                            boolean isFirstResource) {

                        holder.loadingGif.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {

            Intent intent = new Intent(context, FullScreenActivity.class);
            intent.putExtra("list", (Serializable) new ArrayList<>(list));
            intent.putExtra("position", holder.getBindingAdapterPosition());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void filter(String category) {

        list.clear();

        if (category == null || category.equalsIgnoreCase("all")) {
            list.addAll(fullList);
        } else {

            for (WallpaperModel model : fullList) {

                if (model.getCategory() != null &&
                        model.getCategory().equalsIgnoreCase(category)) {

                    list.add(model);
                }
            }
        }

        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ImageView loadingGif;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            loadingGif = itemView.findViewById(R.id.loadingGif);
        }
    }
}