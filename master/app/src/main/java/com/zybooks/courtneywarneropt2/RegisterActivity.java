package com.zybooks.courtneywarneropt2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Handles user registration and account creation
public class RegisterActivity extends AppCompatActivity {

    private EditText etNewUsername, etNewPassword;
    private Button btnCreateAccount, btnCancel;
    private EventDatabaseHelper eventDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize UI components
        etNewUsername = findViewById(R.id.etNewUsername);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnCancel = findViewById(R.id.btnCancel);
        eventDbHelper = new EventDatabaseHelper(this);

        // Handle account creation
        btnCreateAccount.setOnClickListener(view -> {
            String newUsername = etNewUsername.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();

            // Validate input fields
            if (newUsername.isEmpty()) {
                etNewUsername.setError("Enter a username");
                return;
            }

            if (newPassword.isEmpty()) {
                etNewPassword.setError("Enter a password");
                return;
            }

            // Attempt user registration
            if (eventDbHelper.registerUser(newUsername, newPassword)) {
                Toast.makeText(RegisterActivity.this, "Account Created! Please Log In.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "Username already exists. Choose another.", Toast.LENGTH_LONG).show();
            }
        });

        // Cancel registration and return to previous screen
        btnCancel.setOnClickListener(view -> finish());
    }
}
