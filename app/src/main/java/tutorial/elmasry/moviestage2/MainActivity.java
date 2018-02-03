package tutorial.elmasry.moviestage2;

import android.content.Intent;
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

import tutorial.elmasry.moviestage2.model.MovieInfo;
import tutorial.elmasry.moviestage2.utilities.HelperUtils;
import tutorial.elmasry.moviestage2.utilities.NetworkUtils;
import tutorial.elmasry.moviestage2.utilities.TheMovieDBJsonUtils;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String MOVIE_INFO_ARRAY_KEY = "movie-info-array";

    private MovieInfo[] mMovieInfoArray;
    private MovieAdapter mMovieAdapter;
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingIndicator;
    private TextView mErrorNoConnectionTv;

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
            mMovieInfoArray = (MovieInfo[]) savedInstanceState.getParcelableArray(MOVIE_INFO_ARRAY_KEY);
            mMovieAdapter.setMovieInfoArray(mMovieInfoArray);
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
        if (mMovieInfoArray != null)
            outState.putParcelableArray(MOVIE_INFO_ARRAY_KEY, mMovieInfoArray);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_popular_movies:
                loadMoviesData(NetworkUtils.SORT_POPULAR);
                return true;
            case R.id.action_top_rated_movies:
                loadMoviesData(NetworkUtils.SORT_TOP_RATED);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMoviesData(int sortBy) {

        if (HelperUtils.isDeviceOnline(this)) {
            hideErrorNoConnectionView();
            // getting rid of old grid view if exist
            mMovieAdapter.setMovieInfoArray(null);
            new FetchMovieInfo().execute(sortBy);
        } else {
            showErrorNoConnectionView();
        }

    }

    @Override
    public void clickHandler(MovieInfo movieInfo) {

        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra(DetailActivity.EXTRA_MOVIE_INFO, movieInfo);
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
                mMovieInfoArray = TheMovieDBJsonUtils.getMovieInfoArrayFromJson(jsonResponse);
                mMovieAdapter.setMovieInfoArray(mMovieInfoArray);
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
