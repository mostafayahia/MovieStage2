package tutorial.elmasry.moviestage2;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import tutorial.elmasry.moviestage2.model.BasicMovieInfo;
import tutorial.elmasry.moviestage2.utilities.HelperUtils;
import tutorial.elmasry.moviestage2.utilities.NetworkUtils;
import tutorial.elmasry.moviestage2.utilities.TheMovieDBJsonUtils;
import tutorial.elmasry.moviestage2.data.FavouriteMovieContract.FavouriteMovieEntry;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String MOVIE_INFO_ARRAY_KEY = "movie-info-array";

    private BasicMovieInfo[] mBasicMovieInfoArray;
    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorNoConnectionTv;

    /*
     * we use this variable to detect if the current display in this activity is favourites or not
     */
    private boolean mFavouritesDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);
        mErrorNoConnectionTv = findViewById(R.id.tv_error_no_connection);

        mMovieAdapter = new MovieAdapter(this, this);

        mRecyclerView = findViewById(R.id.rv_poster);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setReverseLayout(false);

        mRecyclerView.setLayoutManager(gridLayoutManager);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerView.setAdapter(mMovieAdapter);

        if (null != savedInstanceState && savedInstanceState.containsKey(MOVIE_INFO_ARRAY_KEY)) {
            mBasicMovieInfoArray = (BasicMovieInfo[]) savedInstanceState.getParcelableArray(MOVIE_INFO_ARRAY_KEY);
            mMovieAdapter.setBasicMovieInfoArray(mBasicMovieInfoArray);
        } else {
            loadMoviesData(NetworkUtils.SORT_POPULAR);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.movie, menu);
        return true;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        if (mBasicMovieInfoArray != null)
            outState.putParcelableArray(MOVIE_INFO_ARRAY_KEY, mBasicMovieInfoArray);

    }

    @Override
    protected void onStart() {

        super.onStart();

        /*
         * it is useful to reload movies from favourites database in case of the user unfavourites
         * the movie from the detail activity then return back to this activity while favourites
         * is currently displayed
         */
        if (mFavouritesDisplay)
            loadFavouriteMoviesDataFromFavouritesDb();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        mFavouritesDisplay = false;

        switch (itemId) {
            case R.id.action_popular_movies:
                loadMoviesData(NetworkUtils.SORT_POPULAR);
                return true;
            case R.id.action_top_rated_movies:
                loadMoviesData(NetworkUtils.SORT_TOP_RATED);
                return true;
            case R.id.action_favourites:
                loadFavouriteMoviesDataFromFavouritesDb();
                mFavouritesDisplay = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadFavouriteMoviesDataFromFavouritesDb() {

        hideErrorNoConnectionView();

        // getting from any movies' posters if exist
        mMovieAdapter.setBasicMovieInfoArray(null);

        final String[] PROJECTION = {
                FavouriteMovieEntry._ID,
                FavouriteMovieEntry.COLUMN_TITLE,
                FavouriteMovieEntry.COLUMN_POSTER_URL,
                FavouriteMovieEntry.COLUMN_RELEASE_DATE,
                FavouriteMovieEntry.COLUMN_USER_RATING,
                FavouriteMovieEntry.COLUMN_PLOT_SYNOPSIS
        };

        final int INDEX_ID = 0;
        final int INDEX_TITLE = 1;
        final int INDEX_POSTER_URL = 2;
        final int INDEX_RELEASE_DATE = 3;
        final int INDEX_USER_RATING = 4;
        final int INDEX_PLOT_SYNOPSIS = 5;

        Cursor cursor = getContentResolver().query(
                FavouriteMovieEntry.CONTENT_URI,
                PROJECTION, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {

            BasicMovieInfo[] basicMovieInfoArray = new BasicMovieInfo[cursor.getCount()];

            while (cursor.moveToNext()) {

                BasicMovieInfo basicMovieInfo = new BasicMovieInfo();
                basicMovieInfo.setId(cursor.getInt(INDEX_ID));
                basicMovieInfo.setOriginalTitle(cursor.getString(INDEX_TITLE));
                basicMovieInfo.setPosterUrl(cursor.getString(INDEX_POSTER_URL));
                basicMovieInfo.setReleaseDate(cursor.getString(INDEX_RELEASE_DATE));
                basicMovieInfo.setUserRating(cursor.getDouble(INDEX_USER_RATING));
                basicMovieInfo.setPlotSynopsis(cursor.getString(INDEX_PLOT_SYNOPSIS));

                basicMovieInfoArray[cursor.getPosition()] = basicMovieInfo;
            }

            mBasicMovieInfoArray = basicMovieInfoArray;
            mMovieAdapter.setBasicMovieInfoArray(mBasicMovieInfoArray);
        }
    }

    private void loadMoviesData(int sortBy) {

        if (HelperUtils.isDeviceOnline(this)) {
            hideErrorNoConnectionView();
            // getting rid of old grid view if exist
            mMovieAdapter.setBasicMovieInfoArray(null);
            new FetchMovieInfo().execute(sortBy);
        } else {
            showErrorNoConnectionView();
        }

    }

    @Override
    public void clickHandler(BasicMovieInfo basicMovieInfo) {

        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra(DetailActivity.INTENT_EXTRA_MOVIE_INFO_BASIC, basicMovieInfo);
        startActivity(detailIntent);

    }


    private class FetchMovieInfo extends AsyncTask<Integer, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Integer... integers) {

            int sortBy = integers[0];

            try {

                return NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrl(sortBy));

            } catch (IOException e) {

                e.printStackTrace();
                Log.e(LOG_TAG, "error in getting json response for movies");
                return null;

            }

        }

        @Override
        protected void onPostExecute(String jsonResponse) {

            mLoadingIndicator.setVisibility(View.INVISIBLE);

            if (null == jsonResponse) return;

            try {
                mBasicMovieInfoArray = TheMovieDBJsonUtils.getMovieInfoArrayFromJson(jsonResponse);
                mMovieAdapter.setBasicMovieInfoArray(mBasicMovieInfoArray);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "error in getting movie info array from json response");
                e.printStackTrace();
            }
        }
    }


    private void showErrorNoConnectionView() {
        mErrorNoConnectionTv.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void hideErrorNoConnectionView() {
        mErrorNoConnectionTv.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

}
