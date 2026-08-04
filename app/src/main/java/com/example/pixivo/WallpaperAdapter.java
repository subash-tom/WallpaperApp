package com.example.pixivo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.ViewHolder> {

    Activity context;
    List<WallpaperModel> list;
    List<WallpaperModel> fullList;

    public WallpaperAdapter(Activity context, List<WallpaperModel> list) {
        this.context = context;
        this.list = new ArrayList<>();
        this.fullList = new ArrayList<>();

        if (list != null) {
            this.list.addAll(list);
            this.fullList.addAll(list);
        }
    }

    public void updateFullList(List<WallpaperModel> data) {

        list.clear();
        fullList.clear();

        list.addAll(data);
        fullList.addAll(data);

        Log.d("ADAPTER_DEBUG", "Items = " + list.size());

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

        WallpaperModel model = list.get(position);

        Log.d("BIND", "URL = " + model.getImageUrl());

        holder.loadingGif.setVisibility(View.GONE);

        Glide.with(context)
                .load(model.getImageUrl())
                .error(R.drawable.ic_launcher_background)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(v -> {

            Intent intent = new Intent(context, FullScreenActivity.class);

            intent.putExtra("list",
                    (Serializable) new ArrayList<>(list));

            intent.putExtra("position", position);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {

        Log.d("ITEM_COUNT", String.valueOf(list.size()));

        return list.size();
    }

    public void filter(String category) {

        list.clear();

        if (category == null ||
                category.equalsIgnoreCase("all")) {

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView,loadingGif;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);



            imageView =
                    itemView.findViewById(R.id.imageView);
            loadingGif = itemView.findViewById(R.id.loadingGif);
        }
    }
}