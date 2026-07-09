package com.example.pixivo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;

    TextView registerText;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        TextView signInText = findViewById(R.id.registerText);

        String text = "Not A Member? Register Now";
        SpannableString spannable = new SpannableString(text);

// set color only for "Sign In"
        spannable.setSpan(
                new ForegroundColorSpan(Color.RED),  // change color here
                text.indexOf("Register Now"),
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        };

        spannable.setSpan(
                clickableSpan,
                text.indexOf("Register Now"),
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        signInText.setMovementMethod(LinkMovementMethod.getInstance());

        signInText.setText(spannable);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerText = findViewById(R.id.registerText);

        auth = FirebaseAuth.getInstance();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });



        SharedPreferences pref = getSharedPreferences("MyApp", MODE_PRIVATE);
        boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // 🔥 Already logged in → go Home
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
        }

        // 🔥 Auto-fill email from Register page
        String getEmail = getIntent().getStringExtra("email");
        if (getEmail != null) {
            email.setText(getEmail);
        }

        loginBtn.setOnClickListener(v -> {

            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();

            // ✅ Empty validation
            if (userEmail.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Email format check
            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                email.setError("Enter valid email");
                return;
            }

            // ✅ Password length
            if (userPassword.length() < 6) {
                password.setError("Password must be 6+ characters");
                return;
            }

            // 🔥 Firebase Login
            auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                            // 🚀 Go to Home Page
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();

                        } else {

                            // 🔥 Show real error
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        });

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