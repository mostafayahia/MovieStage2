package tutorial.elmasry.moviestage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import tutorial.elmasry.moviestage2.data.FavouriteMovieContract.FavouriteMovieEntry;

/**
 * Created by yahia on 2/9/18.
 */

class FavouriteMovieDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movie.db";

    private static final int VERSION = 1;

    public FavouriteMovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE " + FavouriteMovieEntry.TABLE_NAME + " (" +
                FavouriteMovieEntry._ID + " INTEGER PRIMARY KEY, " +
                FavouriteMovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_POSTER_URL + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_RUNNING_TIME + " INTEGER NOT NULL, " +
                FavouriteMovieEntry.COLUMN_USER_RATING + " REAL NOT NULL, " +
                FavouriteMovieEntry.COLUMN_PLOT_SYNOPSIS + " TEXT NOT NULL, " +
                FavouriteMovieEntry.COLUMN_TRAILER1_URL + " TEXT DEFAULT '', " +
                FavouriteMovieEntry.COLUMN_TRAILER2_URL + " TEXT DEFAULT '', " +
                FavouriteMovieEntry.COLUMN_REVIEWS_IN_HTML + " TEXT DEFAULT '');";

        db.execSQL(CREATE_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavouriteMovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
