package com.example.pixivo;

import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.yalantis.ucrop.UCrop;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class FullScreenActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    ArrayList<WallpaperModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);



        // 🔹 Views
        viewPager = findViewById(R.id.viewPager);
        ImageView downloadBtn = findViewById(R.id.downloadBtn);
        ImageView wallpaperBtn = findViewById(R.id.wallpaperBtn);
        ImageView backBtn = findViewById(R.id.backBtn);
        ImageView favBtn = findViewById(R.id.favBtn);


        // 🔹 Back
        backBtn.setOnClickListener(v -> finish());

        // 🔹 Data
        list = (ArrayList<WallpaperModel>) getIntent().getSerializableExtra("list");
        int position = getIntent().getIntExtra("position", 0);

        // 🔹 ViewPager
        // 🔹 ViewPager
        FullScreenAdapter adapter = new FullScreenAdapter(list);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(position, false);

// ❤️ Check favourite when activity opens
        checkFavourite(position, favBtn);

// ❤️ Check favourite when user swipes
        viewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        checkFavourite(position, favBtn);
                    }
                }
        );
        // ================= DOWNLOAD =================
        downloadBtn.setOnClickListener(v -> {
            int currentPosition = viewPager.getCurrentItem();
            String imageUrl = list.get(currentPosition).getImageUrl();
            downloadImage(imageUrl);
        });

        // ================= WALLPAPER =================
        wallpaperBtn.setOnClickListener(v -> showWallpaperOptions());

        // ================= ❤️ FAVOURITE =================
        favBtn.setOnClickListener(v -> {

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(
                        FullScreenActivity.this,
                        "Please Login First",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            String uid = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();

            int currentPosition = viewPager.getCurrentItem();

            WallpaperModel wallpaper = list.get(currentPosition);

            String imageUrl = wallpaper.getImageUrl();
            String category = wallpaper.getCategory();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("users")
                    .document(uid)
                    .collection("favourites")
                    .whereEqualTo("imageUrl", imageUrl)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            // REMOVE FAVOURITE
                            String docId =
                                    queryDocumentSnapshots
                                            .getDocuments()
                                            .get(0)
                                            .getId();

                            db.collection("users")
                                    .document(uid)
                                    .collection("favourites")
                                    .document(docId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {

                                        favBtn.setColorFilter(
                                                android.graphics.Color.WHITE
                                        );

                                        Toast.makeText(
                                                FullScreenActivity.this,
                                                "Removed From Favourite 🤍",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });

                        } else {

                            // ADD FAVOURITE
                            Map<String, Object> data = new HashMap<>();
                            data.put("imageUrl", imageUrl);
                            data.put("category", category);
                            data.put("timestamp", System.currentTimeMillis());

                            db.collection("users")
                                    .document(uid)
                                    .collection("favourites")
                                    .add(data)
                                    .addOnSuccessListener(documentReference -> {

                                        favBtn.setColorFilter(
                                                android.graphics.Color.RED
                                        );

                                        Toast.makeText(
                                                FullScreenActivity.this,
                                                "Added To Favourite ❤️",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    });
                        }
                    });
        });
    }
    // ================= DOWNLOAD =================
    private void downloadImage(String imageUrl) {
        Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();

                String savedImageURL = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        bitmap,
                        "Pixivo_" + System.currentTimeMillis(),
                        "Wallpaper"
                );

                runOnUiThread(() -> {
                    if (savedImageURL != null) {
                        Toast.makeText(this, "Saved to Gallery ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed ❌", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Error ❌", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    // ================= POPUP =================
    private void showWallpaperOptions() {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_wallpaper);

        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);

        Button homeBtn = dialog.findViewById(R.id.homeBtn);
        Button lockBtn = dialog.findViewById(R.id.lockBtn);
        Button bothBtn = dialog.findViewById(R.id.bothBtn);

        homeBtn.setOnClickListener(v -> {

            int position = viewPager.getCurrentItem();
            String imageUrl = list.get(position).getImageUrl();

            startCrop(imageUrl, "home");

            dialog.dismiss();
        });

        lockBtn.setOnClickListener(v -> {

            int position = viewPager.getCurrentItem();
            String imageUrl = list.get(position).getImageUrl();

            startCrop(imageUrl, "lock");

            dialog.dismiss();
        });

        bothBtn.setOnClickListener(v -> {

            int position = viewPager.getCurrentItem();
            String imageUrl = list.get(position).getImageUrl();

            startCrop(imageUrl, "both");

            dialog.dismiss();
        });

        dialog.show();
    }
    private String wallpaperType = "home";

    private void startCrop(String imageUrl, String type) {

        wallpaperType = type;

        Uri sourceUri = Uri.parse(imageUrl);

        Uri destinationUri = Uri.fromFile(
                new File(
                        getCacheDir(),
                        "cropped_" + System.currentTimeMillis() + ".jpg"
                )
        );

        DisplayMetrics dm = getResources().getDisplayMetrics();

        UCrop.Options options = new UCrop.Options();

        options.setFreeStyleCropEnabled(true);
        options.setToolbarTitle("Crop Wallpaper");
        options.setHideBottomControls(false);
        options.setShowCropFrame(true);
        options.setShowCropGrid(true);


        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(
                        dm.widthPixels,
                        dm.heightPixels
                )
                .withMaxResultSize(
                        dm.widthPixels,
                        dm.heightPixels
                )
                .withOptions(options)
                .start(this);
    }
    // ================= SET WALLPAPER =================
    private void setWallpaper(String imageUrl, String type) {
        Toast.makeText(this, "Setting wallpaper...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Bitmap bitmap = Glide.with(this)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get();

                WallpaperManager manager = WallpaperManager.getInstance(this);

                if (type.equals("home")) {
                    manager.setBitmap(bitmap);
                } else if (type.equals("lock")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                    }
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                    } else {
                        manager.setBitmap(bitmap);
                    }
                }

                runOnUiThread(() ->
                        Toast.makeText(this, "Wallpaper Set ✅", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed ❌", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {

            Uri resultUri = UCrop.getOutput(data);

            if (resultUri == null) {
                Toast.makeText(this, "Crop Failed!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(),
                        resultUri
                );

                WallpaperManager manager = WallpaperManager.getInstance(this);

                if (wallpaperType.equals("home")) {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                        manager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_SYSTEM
                        );

                    } else {

                        manager.setBitmap(bitmap);
                    }

                } else if (wallpaperType.equals("lock")) {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                        manager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_LOCK
                        );

                    } else {

                        manager.setBitmap(bitmap);
                    }

                } else if (wallpaperType.equals("both")) {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

                        manager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_SYSTEM
                        );

                        manager.setBitmap(
                                bitmap,
                                null,
                                true,
                                WallpaperManager.FLAG_LOCK
                        );

                    } else {

                        manager.setBitmap(bitmap);
                    }
                }

                Toast.makeText(
                        this,
                        "Wallpaper Set Successfully ✅",
                        Toast.LENGTH_SHORT
                ).show();

            } catch (Exception e) {

                e.printStackTrace();

                Toast.makeText(
                        this,
                        "Error : " + e.toString(),
                        Toast.LENGTH_LONG
                ).show();
            }

        } else if (resultCode == UCrop.RESULT_ERROR) {

            Throwable cropError = UCrop.getError(data);

            Toast.makeText(
                    this,
                    cropError != null ? cropError.toString() : "Crop Error",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
    private void checkFavourite(int position, ImageView favBtn) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            favBtn.setColorFilter(android.graphics.Color.WHITE);
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        String imageUrl = list.get(position).getImageUrl();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("favourites")
                .whereEqualTo("imageUrl", imageUrl)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        // ❤️ Favourite exists
                        favBtn.setColorFilter(
                                android.graphics.Color.RED
                        );

                    } else {

                        // 🤍 Not favourite
                        favBtn.setColorFilter(
                                android.graphics.Color.WHITE
                        );
                    }
                });
    }
}