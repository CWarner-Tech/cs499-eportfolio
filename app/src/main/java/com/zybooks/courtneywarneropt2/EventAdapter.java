package com.zybooks.courtneywarneropt2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adapter for displaying a list of events in a RecyclerView
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<Event> eventList;//Marked as final
    private final Context context;//Marked as final
    private final EventDatabaseHelper dbHelper;//Marked as final
    private final int userId;//Marked as final
    // Marked as final
    private final Runnable emptyListCallback; // Callback for handling empty list

    // Constructor to initialize event list, context, and database helper
    public EventAdapter(List<Event> eventList, Context context, int userId, Runnable emptyListCallback) {
        this.eventList = eventList;
        this.context = context;
        this.userId = userId;
        this.dbHelper = new EventDatabaseHelper(context);
        this.emptyListCallback = emptyListCallback;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate event item layout
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvEventName.setText(event.getName());

        // Use string resources for "Date: " and "Time: "
        holder.tvEventDate.setText(context.getString(R.string.event_date, event.getDate()));
        holder.tvEventTime.setText(context.getString(R.string.event_time, event.getTime()));

        // Edit event on button click
        holder.btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditEventActivity.class);
            intent.putExtra("event_id", event.getId());
            intent.putExtra("user_id", userId);
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_date", event.getDate());
            intent.putExtra("event_time", event.getTime());
            context.startActivity(intent);
        });

        // Show delete confirmation dialog
        holder.btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmationDialog(event, position));
    }

    // Displays a confirmation dialog before deleting an event
    private void showDeleteConfirmationDialog(Event event, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Use string resources for delete event
        builder.setTitle(context.getString(R.string.delete_event_title))
                .setMessage(context.getString(R.string.delete_event_message))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> deleteEvent(event, position))
                .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    // Deletes an event and updates the RecyclerView
    private void deleteEvent(Event event, int position) {
        boolean isDeleted = dbHelper.deleteEvent(event.getId(), userId);
        if (isDeleted) {
            eventList.remove(position);
            notifyItemRemoved(position);

            //Use string resource for "Event deleted"
            Toast.makeText(context, context.getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();

            // Notify if the list is empty
            if (eventList.isEmpty()) {
                emptyListCallback.run();
            }
        } else {
            //Use string resource for "Failed to delete event"
            Toast.makeText(context, context.getString(R.string.event_delete_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder class for event items
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventTime;
        Button btnEditEvent, btnDeleteEvent;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}
