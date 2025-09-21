package com.zybooks.courtneywarneropt2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

// Handles user login and navigation to event list or registration
public class LoginActivity extends AppCompatActivity {

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

        // Handle login attempt
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Verify user credentials
            int userId = userDbHelper.getUserId(username, password);
            if (userId != -1) {
                //Use string resource
                Toast.makeText(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, EventListActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
                finish();
            } else {
                // Show dialog prompting account creation

                //Use string resources for dialog
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
}
