package com.example.pixivo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;

public class LatestActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    ArrayList<WallpaperModel> list;
    WallpaperAdapter adapter;

    ImageView homeBtn, favouriteBtn, latestBtn, profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);


        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        cm.registerDefaultNetworkCallback(
                new ConnectivityManager.NetworkCallback() {

                    @Override
                    public void onLost(Network network) {
                        runOnUiThread(() -> showNetworkDialog());
                    }
                }
        );


        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeColors(
                Color.BLACK,
                Color.BLUE,
                Color.RED
        );

        swipeRefresh.setOnRefreshListener(() -> {

            swipeRefresh.setRefreshing(true);

            loadLatestWallpapers();

        });

        homeBtn = findViewById(R.id.homeBtn);
        favouriteBtn = findViewById(R.id.favouriteBtn);
        latestBtn = findViewById(R.id.latestBtn);
        profileBtn = findViewById(R.id.profileBtn);

        list = new ArrayList<>();
        adapter = new WallpaperAdapter(this, list);

        int screenWidthDp = getResources().getConfiguration().screenWidthDp;

        int spanCount;

        if (screenWidthDp >= 900) {
            spanCount = 5; // Large Tablet
        } else if (screenWidthDp >= 600) {
            spanCount = 4; // Tablet
        } else {
            spanCount = 2; // Mobile
        }

        recyclerView.setLayoutManager(
                new GridLayoutManager(this, spanCount)
        );


        int space = (int) (4 * getResources()
                .getDisplayMetrics()
                .density);

        recyclerView.addItemDecoration(
                new RecyclerSpace(spanCount, space, true)
        );

        // 🔥 NAV COLORS
        homeBtn.setColorFilter(Color.GRAY);
        latestBtn.setColorFilter(Color.BLACK);
        favouriteBtn.setColorFilter(Color.GRAY);
        profileBtn.setColorFilter(Color.GRAY);

        // 🔹 Navigation Clicks
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LatestActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        favouriteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LatestActivity.this, FavouriteActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LatestActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        ShimmerAdapter shimmerAdapter = new ShimmerAdapter();

        recyclerView.setAdapter(shimmerAdapter);

        recyclerView.post(() -> {
            recyclerView.setAdapter(adapter);
        });

        // 🔥 LOAD FIRESTORE DATA
        loadLatestWallpapers();
    }

    // 🔥 FIXED LATEST LOGIC (SAFE VERSION)
    private void loadLatestWallpapers() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();



        list.clear();

        db.collection("wallpapers")
                .get()
                .addOnSuccessListener(allDocuments -> {

                    HashMap<String, Long> latestBatchMap = new HashMap<>();

                    // STEP 1 - Find latest batch of every category
                    for (DocumentSnapshot doc : allDocuments) {

                        String category = doc.getString("category");
                        Long batchId = doc.getLong("batchId");

                        Log.d("FIRESTORE_DATA",
                                "Category = " + category +
                                        " Batch = " + batchId);

                        if (category == null || batchId == null)
                            continue;

                        if (!latestBatchMap.containsKey(category)
                                || batchId > latestBatchMap.get(category)) {

                            latestBatchMap.put(category, batchId);
                        }
                    }

                    // STEP 2 - Add only latest batch images
                    for (DocumentSnapshot doc : allDocuments) {

                        String category = doc.getString("category");
                        Long batchId = doc.getLong("batchId");

                        if (category == null || batchId == null)
                            continue;

                        if (batchId.equals(latestBatchMap.get(category))) {

                            String imageUrl = doc.getString("imageUrl");

                            Log.d("LATEST_SHOW",
                                    "Category = " + category +
                                            " Batch = " + batchId +
                                            " URL = " + imageUrl);

                            if (imageUrl != null) {

                                list.add(new WallpaperModel(
                                        imageUrl,
                                        category
                                ));
                            }
                        }
                    }

                    Log.d("TOTAL_LIST_SIZE",
                            "Images = " + list.size());

                    adapter.updateFullList(list);
                    adapter.notifyDataSetChanged();

                    swipeRefresh.setRefreshing(false);

                })
                .addOnFailureListener(e -> {

                    swipeRefresh.setRefreshing(false);

                    Log.e("LATEST_ERROR", e.getMessage());

                    Toast.makeText(
                            LatestActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();

                });
    }
    private boolean isConnected() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        if (cm == null) return false;

        Network network = cm.getActiveNetwork();

        if (network == null) return false;

        NetworkCapabilities capabilities =
                cm.getNetworkCapabilities(network);

        return capabilities != null &&
                (
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                );
    }
    private Dialog networkDialog;

    private void showNetworkDialog() {

        if (networkDialog != null && networkDialog.isShowing()) {
            return;
        }

        networkDialog = new Dialog(
                this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
        );

        networkDialog.setContentView(R.layout.no_internet);
        networkDialog.setCancelable(false);

        if (networkDialog.getWindow() != null) {
            networkDialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            networkDialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        Button retry = networkDialog.findViewById(R.id.btnRetry);
        Button ok = networkDialog.findViewById(R.id.btnOk);

        retry.setOnClickListener(v -> {

            if (isConnected()) {

                networkDialog.dismiss();

            } else {

                networkDialog.dismiss();

                new android.os.Handler().postDelayed(() -> {
                    showNetworkDialog();
                }, 0); // 0.1 second
            }
        });
        ok.setOnClickListener(v -> {

            networkDialog.dismiss();

            finishAffinity(); // close app completely

            System.exit(0);
        });

        networkDialog.show();
    }
    private void showExitDialog() {

        Dialog dialog = new Dialog(
                this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
        );

        dialog.setContentView(R.layout.exit_dialog);
        dialog.setCancelable(false);

        Button btnExit = dialog.findViewById(R.id.btnExit);
        Button btnCancel = dialog.findViewById(R.id.btnCancel);

        btnExit.setOnClickListener(v -> {
            dialog.dismiss();
            finishAffinity(); // Close app
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            window.setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }
    }
}