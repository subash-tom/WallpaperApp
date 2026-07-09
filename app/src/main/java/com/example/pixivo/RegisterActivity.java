package com.example.pixivo;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.*;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import java.util.HashMap;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email, confirmPassword;
    TextInputEditText password;
    Button registerBtn;
    TextView signInText;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 🔗 Bind views
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.registerBtn);
        signInText = findViewById(R.id.signInText);
        NestedScrollView scrollView = findViewById(R.id.scrollView);

        auth = FirebaseAuth.getInstance();

        // 🔹 SharedPreferences
        SharedPreferences pref = getSharedPreferences("MyApp", MODE_PRIVATE);

        // 🔥 Sign In clickable text
        String text = "Do you have an account? Sign In";
        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(
                new ForegroundColorSpan(Color.RED),
                text.indexOf("Sign In"),
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        };

        spannable.setSpan(
                clickableSpan,
                text.indexOf("Sign In"),
                text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        signInText.setText(spannable);
        signInText.setMovementMethod(LinkMovementMethod.getInstance());

        // 🔥 Keyboard fix
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        );

        // 🔥 Auto scroll
        View.OnFocusChangeListener listener = (v, hasFocus) -> {
            if (hasFocus) {
                scrollView.post(() -> scrollView.smoothScrollTo(0, v.getBottom()));
            }
        };

        username.setOnFocusChangeListener(listener);
        email.setOnFocusChangeListener(listener);
        password.setOnFocusChangeListener(listener);
        confirmPassword.setOnFocusChangeListener(listener);

        // 🔥 Username validation
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String userName = s.toString();

                if (userName.isEmpty()) {
                    username.setError(null);
                    return;
                }

                if (!userName.matches("[a-zA-Z0-9_ ]+")) {
                    username.setError("Only letters & numbers allowed");
                } else {
                    username.setError(null);
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 🔥 Register button
        registerBtn.setOnClickListener(v -> {

            String userName = username.getText().toString().trim();
            String userEmail = email.getText().toString().trim();
            String userPassword = password.getText().toString().trim();
            String userConfirmPassword = confirmPassword.getText().toString().trim();

            if (userName.isEmpty() || userEmail.isEmpty()
                    || userPassword.isEmpty() || userConfirmPassword.isEmpty()) {
                Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userName.length() < 3) {
                username.setError("Min 3 characters");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                email.setError("Enter valid email");
                return;
            }

            if (userPassword.length() < 6) {
                password.setError("Password must be 6+ characters");
                return;
            }

            if (!userPassword.equals(userConfirmPassword)) {
                confirmPassword.setError("Password not match");
                return;
            }

            // 🔥 Firebase register
            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = auth.getCurrentUser().getUid();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            HashMap<String, Object> user = new HashMap<>();
                            user.put("name", userName);
                            user.put("email", userEmail);
                            user.put("image", "");

                            db.collection("users")
                                    .document(uid)
                                    .set(user)
                                    .addOnSuccessListener(unused -> {

                                        SharedPreferences.Editor editor = pref.edit();
                                        editor.putString("username", userName);
                                        editor.apply();

                                        Toast.makeText(
                                                RegisterActivity.this,
                                                "Account Created",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        startActivity(
                                                new Intent(
                                                        RegisterActivity.this,
                                                        LoginActivity.class
                                                )
                                        );

                                        finish();

                                    })
                                    .addOnFailureListener(e -> {

                                        Toast.makeText(
                                                RegisterActivity.this,
                                                e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });


                        } else {
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // 🔙 Back press → dialog
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
    }

    // 🔥 FINAL EXIT DIALOG FIXED
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