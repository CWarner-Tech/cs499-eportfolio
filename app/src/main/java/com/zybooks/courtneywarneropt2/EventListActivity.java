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

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private EventDatabaseHelper dbHelper;
    private TextView tvNoUpcomingEvents;
    private Button btnAddEvent, btnLogout;
    private int userId; // Store userId for database queries

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
        recyclerViewEvents.setNestedScrollingEnabled(true);

        // Get userId from Intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            //Use string resource
            Toast.makeText(this, getString(R.string.user_id_missing), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        eventList = dbHelper.getEventsByUser(userId);

        if (eventList.isEmpty()) {
            tvNoUpcomingEvents.setVisibility(View.VISIBLE);
            recyclerViewEvents.setVisibility(View.GONE);
        } else {
            tvNoUpcomingEvents.setVisibility(View.GONE);
            recyclerViewEvents.setVisibility(View.VISIBLE);

            //Pass a Runnable as the fourth parameter
            eventAdapter = new EventAdapter(eventList, this, userId, this::handleEmptyList);
            recyclerViewEvents.setAdapter(eventAdapter);

            recyclerViewEvents.post(() -> {
                recyclerViewEvents.setVerticalScrollBarEnabled(eventAdapter.getItemCount() > 4);
            });
        }
    }

    private void handleEmptyList() {
        if (eventAdapter.getItemCount() == 0) {
            tvNoUpcomingEvents.setVisibility(View.VISIBLE);
            recyclerViewEvents.setVisibility(View.GONE);
        }
    }

    private void showLogoutConfirmationDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                //Use string resources
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.confirm_logout))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> logoutUser())
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void logoutUser() {
        //Use string resource
        Toast.makeText(EventListActivity.this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(EventListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
