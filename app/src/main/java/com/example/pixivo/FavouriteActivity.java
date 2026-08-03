package com.example.pixivo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.view.WindowCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FavouriteActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefresh;
    ArrayList<WallpaperModel> list;
    WallpaperAdapter adapter;

    ImageView latestBtn, homeBtn, favouriteBtn, profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        setContentView(R.layout.activity_favourite);
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
            loadFavourites();
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

        homeBtn.setColorFilter(Color.GRAY);
        latestBtn.setColorFilter(Color.GRAY);
        favouriteBtn.setColorFilter(Color.BLACK);
        profileBtn.setColorFilter(Color.GRAY);

        // 🔹 Navigation Clicks
        latestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FavouriteActivity.this, LatestActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FavouriteActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        profileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(FavouriteActivity.this, ProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        ShimmerAdapter shimmerAdapter = new ShimmerAdapter();

        recyclerView.setAdapter(shimmerAdapter);

        // 🔥 LOAD FIRESTORE DATA
        loadFavourites();
    }


    private void loadFavourites() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("favourites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String imageUrl = doc.getString("imageUrl");
                        String category = doc.getString("category");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            list.add(new WallpaperModel(imageUrl, category));
                        }
                    }

                    Toast.makeText(
                            FavouriteActivity.this,
                            "Favourites  " + list.size(),
                            Toast.LENGTH_LONG
                    ).show();

                    adapter.updateFullList(list);
                    // Replace shimmer with real images
                    recyclerView.setAdapter(adapter);
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            FavouriteActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    swipeRefresh.setRefreshing(false); // ADD THIS
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