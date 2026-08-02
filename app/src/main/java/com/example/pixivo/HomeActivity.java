package com.example.pixivo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.WindowCompat;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    ImageView homeBtn, latestBtn, favouriteBtn, profileBtn;

    RecyclerView recyclerView;
    boolean isSearchVisible = false;

    private boolean isCategorySelected = false;

    private String selectedCategory = "all";
    SwipeRefreshLayout swipeRefresh;
    ArrayList<WallpaperModel> list;
    WallpaperAdapter adapter;

    // 🔍 Search UI
    TextView appName;
;


    // 🔥 CATEGORY BUTTONS (ADD in XML if not added)
    Button catCar, catBike, catAnimal, catSport, catBird, catFlim, catAnime, catCartoon, catMarvel, catGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);



        // 🔹 Init Views
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeColors(
                Color.BLACK,
                Color.BLUE,
                Color.RED
        );

        swipeRefresh.setOnRefreshListener(() -> {
            loadWallpapers();
        });

        homeBtn = findViewById(R.id.homeBtn);
        favouriteBtn = findViewById(R.id.favouriteBtn);
        latestBtn = findViewById(R.id.latestBtn);
        profileBtn = findViewById(R.id.profileBtn);

        appName = findViewById(R.id.appName);



        // 🔥 CATEGORY BUTTONS
        catCar = findViewById(R.id.catCar);
        catBike = findViewById(R.id.catBike);
        catAnimal = findViewById(R.id.catAnimal);
        catSport = findViewById(R.id.catSport);
        catBird = findViewById(R.id.catBird);
        catFlim = findViewById(R.id.catFlim);
        catMarvel=findViewById(R.id.catMarvel);
        catCartoon=findViewById(R.id.catCartoon);
        catAnime=findViewById(R.id.catAnime);
        catGame=findViewById(R.id.catGame);

//        net work
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


        // 🔹 Recycler Setup
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

        ShimmerAdapter shimmerAdapter = new ShimmerAdapter();

        recyclerView.setAdapter(shimmerAdapter);

        loadWallpapers();
        // 🔥 LOAD FIRESTORE DATA
        loadWallpapers();

        // 🔥 CATEGORY CLICK EVENTS
        catCar.setOnClickListener(v -> {
            selectedCategory = "car";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catBike.setOnClickListener(v -> {
            selectedCategory = "bike";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catAnimal.setOnClickListener(v -> {
            selectedCategory = "animal";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catSport.setOnClickListener(v -> {
            selectedCategory = "sport";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catBird.setOnClickListener(v -> {
            selectedCategory = "bird";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });
        catFlim.setOnClickListener(v -> {
            selectedCategory = "flim";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catAnime.setOnClickListener(v -> {
            selectedCategory = "anime";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catCartoon.setOnClickListener(v -> {
            selectedCategory = "cartoon";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catMarvel.setOnClickListener(v -> {
            selectedCategory = "marvel";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        catGame.setOnClickListener(v -> {
            selectedCategory = "game";
            isCategorySelected = true;
            adapter.filter(selectedCategory);
        });

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (isCategorySelected) {

                            adapter.filter("all");
                            selectedCategory = "all";
                            isCategorySelected = false;

                        } else {

                            showExitDialog();

                        }
                    }
                });
        // 🔹 Bottom Nav Colors
        homeBtn.setColorFilter(Color.BLACK);
        latestBtn.setColorFilter(Color.GRAY);
        favouriteBtn.setColorFilter(Color.GRAY);
        profileBtn.setColorFilter(Color.GRAY);

        // 🔹 Navigation Clicks
        latestBtn.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, LatestActivity.class)));

        favouriteBtn.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, FavouriteActivity.class)));

        profileBtn.setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class)));
    }

    // 🔥 FIRESTORE LOAD
    private void loadWallpapers() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("wallpapers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    list.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        String url = doc.getString("imageUrl");
                        String category = doc.getString("category");

                        if (url != null && !url.isEmpty()) {
                            list.add(new WallpaperModel(url, category));
                        }
                    }

                    // Update adapter
                    adapter.updateFullList(list);

                    // Replace shimmer with real images
                    recyclerView.setAdapter(adapter);

                    adapter.filter(selectedCategory);

                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                })
                .addOnFailureListener(e -> {

                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }

                    Toast.makeText(
                            HomeActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
    // 🔥 EXIT DIALOG
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
    }}