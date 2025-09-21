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
                //Use string resource
                etNewUsername.setError(getString(R.string.error_enter_username));
                return;
            }

            if (newPassword.isEmpty()) {
                //Use string resource
                etNewPassword.setError(getString(R.string.error_enter_password));
                return;
            }

            // Attempt user registration
            if (eventDbHelper.registerUser(newUsername, newPassword)) {
                // Use string resource
                Toast.makeText(RegisterActivity.this, getString(R.string.account_created), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Use string resource
                Toast.makeText(RegisterActivity.this, getString(R.string.username_exists), Toast.LENGTH_LONG).show();
            }
        });

        // Cancel registration and return to previous screen
        btnCancel.setOnClickListener(view -> finish());
    }
}
