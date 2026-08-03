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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    ImageView homeBtn, latestBtn, favouriteBtn, profileBtn;
    ImageView profileImage;
    SwipeRefreshLayout swipeRefresh;
    TextView profileName;
    Button logoutBtn;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);

        swipeRefresh = findViewById(R.id.swipeRefresh);

        swipeRefresh.setColorSchemeColors(
                Color.BLACK,
                Color.BLUE,
                Color.RED
        );

        swipeRefresh.setOnRefreshListener(() -> {
            loadUserData();
        });
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

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById(R.id.profileName);
        logoutBtn = findViewById(R.id.logoutBtn);

        homeBtn = findViewById(R.id.homeBtn);
        latestBtn = findViewById(R.id.latestBtn);
        favouriteBtn = findViewById(R.id.favouriteBtn);
        profileBtn = findViewById(R.id.profileBtn);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        homeBtn.setColorFilter(Color.GRAY);
        latestBtn.setColorFilter(Color.GRAY);
        favouriteBtn.setColorFilter(Color.GRAY);
        profileBtn.setColorFilter(Color.BLACK);

        loadUserData();

        logoutBtn.setOnClickListener(v -> {

            auth.signOut();

            Intent intent = new Intent(
                    ProfileActivity.this,
                    LoginActivity.class
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });

        // 🔹 Navigation Clicks
        latestBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LatestActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        favouriteBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FavouriteActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                });
    }

    private void loadUserData() {

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = auth.getCurrentUser().getUid();

//        Toast.makeText(this, "UID: " + uid, Toast.LENGTH_LONG).show();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name = documentSnapshot.getString("name");
                        String image = documentSnapshot.getString("image");



                        if (name != null && !name.isEmpty()) {
                            profileName.setText(name);
                        } else {
                            profileName.setText("No Name");
                        }

                        if (image != null && !image.isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(image)
                                    .into(profileImage);
                        }

                        swipeRefresh.setRefreshing(false);
                    } else {

                        Toast.makeText(
                                ProfileActivity.this,
                                "Document Not Found",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                    swipeRefresh.setRefreshing(false);
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