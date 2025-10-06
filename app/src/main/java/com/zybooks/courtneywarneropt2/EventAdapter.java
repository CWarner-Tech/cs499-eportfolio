package com.zybooks.courtneywarneropt2;

import android.app.AlertDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * EventAdapter is responsible for displaying Event objects in a RecyclerView.
 * Enhancement for Milestone Three (Algorithms & Data Structures):
 * - Uses ListAdapter with DiffUtil for efficient list updates.
 * - Cancels any scheduled reminders if the corresponding event is deleted.
 */
public class EventAdapter extends ListAdapter<Event, EventAdapter.EventViewHolder> {

    private final Context context;
    private final EventDatabaseHelper dbHelper;
    private final int userId;
    private final Runnable emptyListCallback;

    /**
     * DiffUtil.ItemCallback:
     * Defines how to compare Event objects when the list changes.
     */
    private static final DiffUtil.ItemCallback<Event> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Event>() {
                @Override
                public boolean areItemsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Event oldItem, @NonNull Event newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getDate().equals(newItem.getDate())
                            && oldItem.getTime().equals(newItem.getTime());
                }
            };

    public EventAdapter(Context context, int userId, Runnable emptyListCallback) {
        super(DIFF_CALLBACK);
        this.context = context;
        this.userId = userId;
        this.dbHelper = new EventDatabaseHelper(context);
        this.emptyListCallback = emptyListCallback;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = getItem(position);

        // Display event name
        holder.tvEventName.setText(event.getName());

        //Convert epoch values (stored as Strings) back to readable date/time
        try {
            long dateEpoch = Long.parseLong(event.getDate());
            long timeEpoch = Long.parseLong(event.getTime());

            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US);
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US);

            String formattedDate = dateFormat.format(new java.util.Date(dateEpoch));
            String formattedTime = timeFormat.format(new java.util.Date(timeEpoch));

            holder.tvEventDate.setText(context.getString(R.string.event_date, formattedDate));
            holder.tvEventTime.setText(context.getString(R.string.event_time, formattedTime));
        } catch (NumberFormatException e) {
            //Fallback for old data (text-based date/time)
            holder.tvEventDate.setText(context.getString(R.string.event_date, event.getDate()));
            holder.tvEventTime.setText(context.getString(R.string.event_time, event.getTime()));
        }

        //Pass formatted data to EditEventActivity
        holder.btnEditEvent.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditEventActivity.class);
            intent.putExtra("event_id", event.getId());
            intent.putExtra("user_id", userId);
            intent.putExtra("event_name", event.getName());
            intent.putExtra("event_date", holder.tvEventDate.getText().toString().replace("Date: ", ""));
            intent.putExtra("event_time", holder.tvEventTime.getText().toString().replace("Time: ", ""));
            context.startActivity(intent);
        });

        // Delete button
        holder.btnDeleteEvent.setOnClickListener(v -> showDeleteConfirmationDialog(event));
    }

    /**
     * Confirmation dialog before deleting.
     */
    private void showDeleteConfirmationDialog(Event event) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_event_title))
                .setMessage(context.getString(R.string.delete_event_message))
                .setPositiveButton(context.getString(R.string.yes), (dialog, which) -> deleteEvent(event))
                .setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Deletes event from DB, cancels reminder, and updates RecyclerView via DiffUtil.
     */
    private void deleteEvent(Event event) {
        boolean isDeleted = dbHelper.deleteEvent(event.getId(), userId);
        if (isDeleted) {
            cancelReminder(event);

            // Create a new list without the deleted event
            List<Event> updatedList = new ArrayList<>(getCurrentList());
            updatedList.remove(event);

            // Submit the new list to DiffUtil
            submitList(updatedList);

            Toast.makeText(context, context.getString(R.string.event_deleted), Toast.LENGTH_SHORT).show();

            if (updatedList.isEmpty()) {
                emptyListCallback.run();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.event_delete_failed), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Cancels a scheduled reminder when an event is deleted.
     */
    private void cancelReminder(Event event) {
        Intent intent = new Intent(context, SmsBroadcastReceiver.class);
        intent.putExtra("eventName", event.getName());
        intent.putExtra("eventDate", event.getDate());
        intent.putExtra("eventTime", event.getTime());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                event.getName().hashCode(), // Same requestCode used in AddEventActivity
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Holds references to UI components for each event row.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName, tvEventDate, tvEventTime;
        Button btnEditEvent, btnDeleteEvent;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tvEventName);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventTime = itemView.findViewById(R.id.tvEventTime);
            btnEditEvent = itemView.findViewById(R.id.btnEditEvent);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}