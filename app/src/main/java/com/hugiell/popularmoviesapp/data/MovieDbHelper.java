package com.hugiell.popularmoviesapp.data;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MovieDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creation and population of tables goes here
        Log.e("DatabaseLog", "Creating database");
        try {
            // Create favorite_movies table
            db.execSQL("CREATE TABLE favorite_movies (_id INTEGER PRIMARY KEY, movie_id TEXT NOT NULL UNIQUE, title TEXT NOT NULL, poster TEXT NOT NULL, release_date TEXT NOT NULL, rating TEXT NOT NULL, overview TEXT NOT NULL, runtime INTEGER);");
            // Create videos table
            db.execSQL("CREATE TABLE videos (_id INTEGER PRIMARY KEY, movie_id TEXT NOT NULL, key TEXT NOT NULL UNIQUE, name TEXT NOT NULL, type TEXT NOT NULL, site TEXT NOT NULL, size INTEGER);");
            // Create user_reviews table
            db.execSQL("CREATE TABLE user_reviews (_id INTEGER PRIMARY KEY, movie_id TEXT NOT NULL, author TEXT NOT NULL UNIQUE, content TEXT NOT NULL);");
        } catch (SQLException e) {
            Log.e("DatabaseLogError", e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop tables, add tables, or do anything else it needs to upgrade to the new schema version.
    }
}
