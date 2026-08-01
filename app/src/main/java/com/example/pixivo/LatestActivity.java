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


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

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

        recyclerView.setAdapter(adapter);

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

        homeBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)));

        favouriteBtn.setOnClickListener(v ->
                startActivity(new Intent(this, FavouriteActivity.class)));

        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        loadLatestWallpapers();
    }

    // 🔥 FIXED LATEST LOGIC (SAFE VERSION)
    private void loadLatestWallpapers() {

        long sevenDaysAgo =
                System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000);

        FirebaseFirestore.getInstance()
                .collection("wallpapers")
                .whereGreaterThanOrEqualTo("timestamp", sevenDaysAgo)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    int count = queryDocumentSnapshots.size();

                    if (count == 0) {

                        Toast.makeText(
                                LatestActivity.this,
                                " Latests 0",
                                Toast.LENGTH_LONG
                        ).show();

                    } else {

                        Toast.makeText(
                                LatestActivity.this,
                                " Latests " + count,
                                Toast.LENGTH_LONG
                        ).show();

                    }

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String imageUrl = doc.getString("imageUrl");
                        String category = doc.getString("category");

                        if (imageUrl != null) {
                            list.add(new WallpaperModel(imageUrl, category));
                        }
                    }

                    adapter.updateFullList(list);
                    adapter.notifyDataSetChanged();

                    swipeRefresh.setRefreshing(false);

                })
                .addOnFailureListener(e -> {

                    swipeRefresh.setRefreshing(false);

                    Toast.makeText(
                            LatestActivity.this,
                            "Error : " + e.getMessage(),
                            Toast.LENGTH_LONG
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