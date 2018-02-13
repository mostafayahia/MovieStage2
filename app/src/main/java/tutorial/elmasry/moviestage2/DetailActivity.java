package tutorial.elmasry.moviestage2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import tutorial.elmasry.moviestage2.databinding.ActivityDetailBinding;
import tutorial.elmasry.moviestage2.model.ExtraMovieInfo;
import tutorial.elmasry.moviestage2.model.BasicMovieInfo;
import tutorial.elmasry.moviestage2.utilities.HelperUtils;
import tutorial.elmasry.moviestage2.utilities.NetworkUtils;
import tutorial.elmasry.moviestage2.utilities.TheMovieDBJsonUtils;
import tutorial.elmasry.moviestage2.data.FavouriteMovieContract.FavouriteMovieEntry;

public class DetailActivity extends AppCompatActivity {

    public static final String INTENT_EXTRA_MOVIE_INFO_BASIC = "movie-info-basic";

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private ExtraMovieInfo mExtraMovieInfo;
    private BasicMovieInfo mBasicMovieInfo;

    private static final String EXTRA_MOVIE_INFO_KEY = "extra-movie-info";
    private static final String IN_FAVOURITES_KEY = "in-favourites";

    /*
     * we will use this variable to reduce querying favourites database to know if this movie is
     * exist in favourites or not
     */
    private boolean mInFavourites;

    private ActivityDetailBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mBasicMovieInfo = extras.getParcelable(INTENT_EXTRA_MOVIE_INFO_BASIC);
        }

        if (extras == null || mBasicMovieInfo == null) {
            HelperUtils.showToast(this, R.string.message_failing_to_get_movie_detail);
            return;
        }

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // extract year from release date, recall release date in format yyyy-mm-dd
        String releaseYear = mBasicMovieInfo.getReleaseDate().trim().substring(0, 4);

        mBinding.detailTitle.setText(mBasicMovieInfo.getOriginalTitle());
        mBinding.detailReleaseDate.setText(releaseYear);
        mBinding.detailUserRating
                .setText(getString(R.string.detail_format_user_rating, mBasicMovieInfo.getUserRating()));
        mBinding.detailPlotSynopsis.setText(mBasicMovieInfo.getPlotSynopsis());

        String posterUrl = mBasicMovieInfo.getPosterUrl();

        if (posterUrl == null || posterUrl.length() == 0)
            throw new RuntimeException("poster url can't be null or empty");

        Picasso.with(this)
                .load(posterUrl)
                .error(R.drawable.poster_error_in_loading_image)
                .into(mBinding.detailIvPoster);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_MOVIE_INFO_KEY))
                mExtraMovieInfo = savedInstanceState.getParcelable(EXTRA_MOVIE_INFO_KEY);
            if (savedInstanceState.containsKey(IN_FAVOURITES_KEY))
                mInFavourites = savedInstanceState.getBoolean(IN_FAVOURITES_KEY);
            populateUIWithExtraInfo();
        } else {
            if (isMovieInFavouritesDb()) {
                mInFavourites = true;
                getExtraMovieInfoFromFavouritesDb();
                populateUIWithExtraInfo();
            } else {
                new FetchExtraMovieInfo().execute(mBasicMovieInfo.getId());
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (mExtraMovieInfo != null)
            outState.putParcelable(EXTRA_MOVIE_INFO_KEY, mExtraMovieInfo);

        outState.putBoolean(IN_FAVOURITES_KEY, mInFavourites);

        super.onSaveInstanceState(outState);

    }

    private void populateUIWithExtraInfo() {

//        Log.d(LOG_TAG, "movie id: " + mExtraMovieInfo.getMovieId());

        mBinding.detailRunningTime.setText(mExtraMovieInfo.getRunningTime() + "min");

        String trailer1Url = mExtraMovieInfo.getTrailer1Url();
        String trailer2Url = mExtraMovieInfo.getTrailer2Url();
        String reviewsInHtml = mExtraMovieInfo.getReviewsInHtml();

        if (trailer1Url != null && trailer1Url.length() > 0) {
            mBinding.detailLabelTrailers.setVisibility(View.VISIBLE);
            mBinding.detailButtonTrailer1.setVisibility(View.VISIBLE);
            mBinding.detailHorizontalRuleAfterTrailer1.setVisibility(View.VISIBLE);
        }

        if (trailer2Url != null && trailer2Url.length() > 0) {
            mBinding.detailButtonTrailer2.setVisibility(View.VISIBLE);
            mBinding.detailHorizontalRuleAfterTrailer2.setVisibility(View.VISIBLE);
        }

        if (reviewsInHtml != null && reviewsInHtml.length() > 0) {
            mBinding.detailLabelReviews.setVisibility(View.VISIBLE);
            mBinding.detailReviews.setVisibility(View.VISIBLE);
            mBinding.detailReviews.setText(HelperUtils.fromHtml(reviewsInHtml));
        }

        if (mInFavourites) {
            mBinding.detailIcFavourite.setImageResource(R.drawable.ic_movie_in_favourites);
        }
        mBinding.detailIcFavourite.setVisibility(View.VISIBLE);

    }

    public void handleTrailerButtonClick(View view) {

        switch (view.getId()) {
            case R.id.detail_button_trailer1:
                Intent trailer1Intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExtraMovieInfo.getTrailer1Url()));
                if (trailer1Intent.resolveActivity(getPackageManager()) != null)
                    startActivity(trailer1Intent);
                else
                    HelperUtils.showToast(this, R.string.message_cannot_play_video);
                break;
            case R.id.detail_button_trailer2:
                Intent trailer2Intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExtraMovieInfo.getTrailer2Url()));
                if (trailer2Intent.resolveActivity(getPackageManager()) != null)
                    startActivity(trailer2Intent);
                else
                    HelperUtils.showToast(this, R.string.message_cannot_play_video);
        }

    }

    public void handleFavouriteButtonClick(View view) {

        if (mInFavourites) {
            deleteMovieFromFavouritesDb();
            mInFavourites = false;
            mBinding.detailIcFavourite.setImageResource(R.drawable.ic_movie_not_in_favourites);
        } else {
            insertMovieInFavouritesDb();
            mInFavourites = true;
            mBinding.detailIcFavourite.setImageResource(R.drawable.ic_movie_in_favourites);
        }

    }

    private boolean isMovieInFavouritesDb() {

        Cursor cursor =
                getContentResolver().query(
                        Uri.withAppendedPath(FavouriteMovieEntry.CONTENT_URI, mBasicMovieInfo.getId() + ""),
                        new String[]{"_id"}, null, null, null);

        boolean isInFavourites = cursor != null && cursor.getCount() == 1;

        if (cursor != null) cursor.close();

        return isInFavourites;
    }

    private void getExtraMovieInfoFromFavouritesDb() {

        final String[] PROJECTION = {
                FavouriteMovieEntry.COLUMN_RUNNING_TIME,
                FavouriteMovieEntry.COLUMN_TRAILER1_URL,
                FavouriteMovieEntry.COLUMN_TRAILER2_URL,
                FavouriteMovieEntry.COLUMN_REVIEWS_IN_HTML
        };

        final int INDEX_RUNNING_TIME = 0;
        final int INDEX_TRAILER1_URL = 1;
        final int INDEX_TRAILER2_URL = 2;
        final int INDEX_REVIEWS_IN_HTML = 3;

        Cursor cursor = getContentResolver().query(
                Uri.withAppendedPath(FavouriteMovieEntry.CONTENT_URI, mBasicMovieInfo.getId() + ""),
                PROJECTION, null, null, null);

        if (cursor != null && cursor.getCount() == 1) {
            cursor.moveToNext();
            mExtraMovieInfo = new ExtraMovieInfo();
            mExtraMovieInfo.setMovieId(mBasicMovieInfo.getId());
            mExtraMovieInfo.setRunningTime(cursor.getInt(INDEX_RUNNING_TIME));
            mExtraMovieInfo.setTrailer1Url(cursor.getString(INDEX_TRAILER1_URL));
            mExtraMovieInfo.setTrailer2Url(cursor.getString(INDEX_TRAILER2_URL));
            mExtraMovieInfo.setReviewsInHtml(cursor.getString(INDEX_REVIEWS_IN_HTML));
        }

        if (cursor != null) cursor.close();
    }

    private void insertMovieInFavouritesDb() {

        ContentValues contentValues = new ContentValues();

        contentValues.put(FavouriteMovieEntry._ID, mBasicMovieInfo.getId());
        contentValues.put(FavouriteMovieEntry.COLUMN_TITLE, mBasicMovieInfo.getOriginalTitle());
        contentValues.put(FavouriteMovieEntry.COLUMN_POSTER_URL, mBasicMovieInfo.getPosterUrl());
        contentValues.put(FavouriteMovieEntry.COLUMN_RELEASE_DATE, mBasicMovieInfo.getReleaseDate());
        contentValues.put(FavouriteMovieEntry.COLUMN_USER_RATING, mBasicMovieInfo.getUserRating());
        contentValues.put(FavouriteMovieEntry.COLUMN_PLOT_SYNOPSIS, mBasicMovieInfo.getPlotSynopsis());

        contentValues.put(FavouriteMovieEntry.COLUMN_RUNNING_TIME, mExtraMovieInfo.getRunningTime());
        contentValues.put(FavouriteMovieEntry.COLUMN_TRAILER1_URL, mExtraMovieInfo.getTrailer1Url());
        contentValues.put(FavouriteMovieEntry.COLUMN_TRAILER2_URL, mExtraMovieInfo.getTrailer2Url());
        contentValues.put(FavouriteMovieEntry.COLUMN_REVIEWS_IN_HTML, mExtraMovieInfo.getReviewsInHtml());

        getContentResolver().insert(FavouriteMovieEntry.CONTENT_URI, contentValues);

        HelperUtils.showToast(this, R.string.message_movie_added_to_favourites);
    }

    private void deleteMovieFromFavouritesDb() {

        getContentResolver().delete(
                Uri.withAppendedPath(FavouriteMovieEntry.CONTENT_URI, mBasicMovieInfo.getId() + ""),
                null, null
        );
    }

    private class FetchExtraMovieInfo extends AsyncTask<Integer, Void, String[]> {

        private final int[] mUrlForArray = {NetworkUtils.URL_FOR_MOVIE_EXTRA_INFO,
                NetworkUtils.URL_FOR_MOVIE_REVIEWS,
                NetworkUtils.URL_FOR_MOVIE_TRAILERS
        };

        private int mMovieId;

        @Override
        protected String[] doInBackground(Integer... integers) {

            mMovieId = integers[0];
            String[] jsonResponseArray = new String[mUrlForArray.length];

            try {

                for (int i = 0; i < jsonResponseArray.length; i++)
                    jsonResponseArray[i] = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrlWithId(mMovieId, mUrlForArray[i]));

                return jsonResponseArray;

            } catch (IOException e) {

                Log.e(LOG_TAG, "error in getting json response after building url with id");
                e.printStackTrace();
                return null;

            }

        }

        @Override
        protected void onPostExecute(String[] jsonResponseArray) {

            if (null == jsonResponseArray) return;

            mExtraMovieInfo = new ExtraMovieInfo();
            mExtraMovieInfo.setMovieId(mMovieId);

            for (int i = 0; i < jsonResponseArray.length; i++) {
                int urlFor = mUrlForArray[i];
                try {
                    switch (urlFor) {
                        case NetworkUtils.URL_FOR_MOVIE_EXTRA_INFO:
                            int runningTime = TheMovieDBJsonUtils.getMovieRunningTimeFromJson(jsonResponseArray[i]);
                            mExtraMovieInfo.setRunningTime(runningTime);
                            break;
                        case NetworkUtils.URL_FOR_MOVIE_REVIEWS:
                            mExtraMovieInfo.setReviewsInHtml(TheMovieDBJsonUtils.getMovieReviewsInHtmlFromJson(jsonResponseArray[i]));
                            break;
                        case NetworkUtils.URL_FOR_MOVIE_TRAILERS:
                            String[] trailerUrlArray = TheMovieDBJsonUtils.getMovieTrailerUrlArrayFromJson(jsonResponseArray[i]);
                            // The max number of trailers per movie can be watched using this app is two trailers only
                            if (trailerUrlArray != null) {
                                mExtraMovieInfo.setTrailer1Url(trailerUrlArray[0]);
                                if (trailerUrlArray.length > 1)
                                    mExtraMovieInfo.setTrailer2Url(trailerUrlArray[1]);
                            }
                            break;
                    }
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "error in getting movie info from json response, the given urlFor: " + urlFor);
                    e.printStackTrace();
                }
            }

            populateUIWithExtraInfo();

        }
    }



}
