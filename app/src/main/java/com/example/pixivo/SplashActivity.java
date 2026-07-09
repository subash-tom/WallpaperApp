package com.example.pixivo;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000;

    private ImageView logo;
    private TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        logo = findViewById(R.id.logo);

        // Logo Animation
        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.setRotation(0f);

        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(360f)
                .setDuration(1800)
                .start();

        // Text Animation
        if (appName != null) {
            appName.setAlpha(0f);

            appName.animate()
                    .alpha(1f)
                    .setDuration(2000)
                    .start();
        }

        new Handler().postDelayed(() -> {

            if (isConnected()) {

                FirebaseAuth auth = FirebaseAuth.getInstance();

                if (auth.getCurrentUser() != null) {

                    startActivity(
                            new Intent(
                                    SplashActivity.this,
                                    HomeActivity.class
                            )
                    );

                } else {

                    startActivity(
                            new Intent(
                                    SplashActivity.this,
                                    LoginActivity.class
                            )
                    );
                }

                finish();

            } else {

                showNoInternetDialog();
            }

        }, SPLASH_TIME);
    }

    private boolean isConnected() {

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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

    private void showNoInternetDialog() {

        Dialog dialog = new Dialog(
                this,
                android.R.style.Theme_Translucent_NoTitleBar_Fullscreen
        );

        dialog.setContentView(R.layout.no_internet);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {

            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );

            dialog.getWindow().setBackgroundDrawableResource(
                    android.R.color.transparent
            );
        }

        Button retry = dialog.findViewById(R.id.btnRetry);
        Button ok = dialog.findViewById(R.id.btnOk);

        retry.setOnClickListener(v -> {

            if (isConnected()) {

                FirebaseAuth auth = FirebaseAuth.getInstance();

                if (auth.getCurrentUser() != null) {

                    startActivity(
                            new Intent(
                                    SplashActivity.this,
                                    HomeActivity.class
                            )
                    );

                } else {

                    startActivity(
                            new Intent(
                                    SplashActivity.this,
                                    LoginActivity.class
                            )
                    );
                }

                dialog.dismiss();
                finish();

            } else {

                dialog.dismiss();
                showNoInternetDialog();
            }
        });

        ok.setOnClickListener(v -> {

            dialog.dismiss();

            finishAffinity(); // Close App
            System.exit(0);   // Optional
        });

        dialog.show();
    }
}