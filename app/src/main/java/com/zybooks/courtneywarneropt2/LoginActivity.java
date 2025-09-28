package com.zybooks.courtneywarneropt2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Handles user login and navigation to event list or registration.
 * Also requests notification permission on Android 13+ so reminders can appear.
 */
public class LoginActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 200;

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private EventDatabaseHelper userDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI components
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        userDbHelper = new EventDatabaseHelper(this);

        // 🔔 Request notification permission (Android 13+)
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }

        // Handle login attempt
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Verify user credentials
            int userId = userDbHelper.getUserId(username, password);
            if (userId != -1) {
                // Login success
                Toast.makeText(LoginActivity.this,
                        getString(R.string.login_success),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                // Invalid login → show dialog prompting account creation
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(getString(R.string.invalid_login))
                        .setMessage(getString(R.string.prompt_create_account))
                        .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        // Navigate to RegisterActivity for account creation
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Handles the result of notification permission requests.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Notifications may not appear without permission",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
