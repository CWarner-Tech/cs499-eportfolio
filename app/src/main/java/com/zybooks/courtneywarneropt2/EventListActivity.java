package com.zybooks.courtneywarneropt2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Displays a list of events for the logged-in user.
 * Uses EventAdapter with DiffUtil for efficient RecyclerView updates.
 */
public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private EventDatabaseHelper dbHelper;
    private TextView tvNoUpcomingEvents;
    private Button btnAddEvent, btnLogout;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        tvNoUpcomingEvents = findViewById(R.id.tvNoUpcomingEvents);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnLogout = findViewById(R.id.btnLogout);
        dbHelper = new EventDatabaseHelper(this);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setHasFixedSize(true);

        // Get userId from Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, getString(R.string.user_id_missing), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize adapter once
        eventAdapter = new EventAdapter(this, userId, this::handleEmptyList);
        recyclerViewEvents.setAdapter(eventAdapter);

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Load events initially
        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    /**
     * Loads events from the database and submits them to the adapter.
     */
    private void loadEvents() {
        List<Event> eventList = dbHelper.getEventsByUser(userId);

        if (eventList.isEmpty()) {
            tvNoUpcomingEvents.setVisibility(View.VISIBLE);
            recyclerViewEvents.setVisibility(View.GONE);
        } else {
            tvNoUpcomingEvents.setVisibility(View.GONE);
            recyclerViewEvents.setVisibility(View.VISIBLE);

            // Pass events into DiffUtil via ListAdapter
            eventAdapter.submitList(eventList);

            recyclerViewEvents.post(() -> {
                recyclerViewEvents.setVerticalScrollBarEnabled(eventAdapter.getItemCount() > 4);
            });
        }
    }

    /**
     * Handles showing the empty message when list is cleared.
     */
    private void handleEmptyList() {
        tvNoUpcomingEvents.setVisibility(View.VISIBLE);
        recyclerViewEvents.setVisibility(View.GONE);
    }

    /**
     * Shows a confirmation dialog for logging out.
     */
    private void showLogoutConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.confirm_logout))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> logoutUser())
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Logs the user out and returns to LoginActivity.
     */
    private void logoutUser() {
        Toast.makeText(EventListActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(EventListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}