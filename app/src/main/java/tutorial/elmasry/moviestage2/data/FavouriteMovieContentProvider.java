package tutorial.elmasry.moviestage2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import tutorial.elmasry.moviestage2.data.FavouriteMovieContract.FavouriteMovieEntry;

/**
 * Created by yahia on 2/9/18.
 */

public class FavouriteMovieContentProvider extends ContentProvider {

    private static final int FAVOURITES = 500;
    private static final int FAVOURITE_WITH_ID = 501;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private SQLiteOpenHelper mFavouriteMovieDbHelper;

    private static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavouriteMovieContract.AUTHORITY, FavouriteMovieContract.PATH_FAVOURITES, FAVOURITES);
        uriMatcher.addURI(FavouriteMovieContract.AUTHORITY, FavouriteMovieContract.PATH_FAVOURITES + "/#", FAVOURITE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {

        mFavouriteMovieDbHelper = new FavouriteMovieDbHelper(getContext());

        return true;
    }


    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        final SQLiteDatabase db = mFavouriteMovieDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor returnCursor;
        switch (match) {
            case FAVOURITES:
                returnCursor = db.query(FavouriteMovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case FAVOURITE_WITH_ID:
                returnCursor = db.query(FavouriteMovieEntry.TABLE_NAME, projection,
                        "_id=?", new String[]{uri.getLastPathSegment()}, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("unknown uri: " + uri);
        }

        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        int match = sUriMatcher.match(uri);

        Uri returnUri;
        switch (match) {
            case FAVOURITES:
                final SQLiteDatabase db = mFavouriteMovieDbHelper.getWritableDatabase();
                long id = db.insert(FavouriteMovieContract.FavouriteMovieEntry.TABLE_NAME, null, values);
                if (id > 0)
                    returnUri = ContentUris.withAppendedId(FavouriteMovieContract.FavouriteMovieEntry.CONTENT_URI, id);
                else
                    throw new SQLException("Failed to insert in: " + uri);
                break;
            default:
                throw new UnsupportedOperationException("unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        final SQLiteDatabase db = mFavouriteMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int rowDeleted;

        switch (match) {
            case FAVOURITE_WITH_ID:
                rowDeleted = db.delete(FavouriteMovieEntry.TABLE_NAME, "_id=?", new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new UnsupportedOperationException("unknown uri: " + uri);
        }

        if (rowDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
