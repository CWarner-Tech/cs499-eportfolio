package com.zybooks.courtneywarneropt2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

// Manages SQLite database operations for both users and events
public class EventDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "event_tracker.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";  // Primary Key
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // Events Table
    private static final String TABLE_EVENTS = "events";
    private static final String COLUMN_EVENT_ID = "event_id";  // Primary Key for Events
    // `COLUMN_USER_ID` should match Users table
    private static final String COLUMN_EVENT_NAME = "name";
    private static final String COLUMN_EVENT_DATE = "date";
    private static final String COLUMN_EVENT_TIME = "time";

    public EventDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_PASSWORD + " TEXT NOT NULL)";
        db.execSQL(createUsersTable);

        // Create Events Table (Fixed)
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + " (" +
                COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +  // Foreign key linking to Users table
                COLUMN_EVENT_NAME + " TEXT, " +
                COLUMN_EVENT_DATE + " TEXT, " +
                COLUMN_EVENT_TIME + " TEXT, " +
                "FOREIGN KEY(" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ") ON DELETE CASCADE)";
        db.execSQL(createEventsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop and recreate tables on upgrade
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Registers a new user
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;  // Returns true if registration is successful
    }

    // Verifies login credentials and gets User ID
    public int getUserId(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }
        cursor.close();
        db.close();
        return userId;  // Returns user ID or -1 if not found
    }

    // Inserts a new event
    public boolean insertEvent(int userId, String name, String date, String time) {
        if (eventExists(userId, name, date, time)) {
            return false;  // Prevent duplicate event
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_EVENT_NAME, name);
        values.put(COLUMN_EVENT_DATE, date);
        values.put(COLUMN_EVENT_TIME, time);

        long result = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return result != -1;
    }

    // Checks if an event already exists
    public boolean eventExists(int userId, String name, String date, String time) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{COLUMN_EVENT_ID},
                COLUMN_USER_ID + "=? AND " + COLUMN_EVENT_NAME + "=? AND " + COLUMN_EVENT_DATE + "=? AND " + COLUMN_EVENT_TIME + "=?",
                new String[]{String.valueOf(userId), name, date, time},
                null, null, null);

        boolean exists = cursor.moveToFirst();  // Returns true if record found
        cursor.close();
        return exists;
    }

    //Retrieves all events for a specific user
    public List<Event> getEventsByUser(int userId) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS,
                new String[]{COLUMN_EVENT_ID, COLUMN_USER_ID, COLUMN_EVENT_NAME, COLUMN_EVENT_DATE, COLUMN_EVENT_TIME},
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COLUMN_EVENT_DATE + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String name = cursor.getString(2);
                String date = cursor.getString(3);
                String time = cursor.getString(4);
                eventList.add(new Event(id, userId, name, date, time));
            }
            cursor.close();
        }
        return eventList;
    }

    //Updates an existing event
    public boolean updateEvent(int eventId, int userId, String name, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, name);
        values.put(COLUMN_EVENT_DATE, date);
        values.put(COLUMN_EVENT_TIME, time);

        int rowsAffected = db.update(TABLE_EVENTS, values, COLUMN_EVENT_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(eventId), String.valueOf(userId)});

        db.close();
        return rowsAffected > 0;
    }

    // Deletes an event
    public boolean deleteEvent(int eventId, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deletedRows = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + "=? AND " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(eventId), String.valueOf(userId)});

        db.close();
        return deletedRows > 0;
    }
}
