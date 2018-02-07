package tutorial.elmasry.moviestage2;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import tutorial.elmasry.moviestage2.model.ExtraMovieInfo;
import tutorial.elmasry.moviestage2.model.MovieInfo;
import tutorial.elmasry.moviestage2.utilities.HelperUtils;
import tutorial.elmasry.moviestage2.utilities.NetworkUtils;
import tutorial.elmasry.moviestage2.utilities.TheMovieDBJsonUtils;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_INFO = "movie-info";
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private ExtraMovieInfo mExtraMovieInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        MovieInfo movieInfo = null;
        if (intent != null) {
            movieInfo = intent.getExtras().getParcelable(EXTRA_MOVIE_INFO);
        }

        if (intent == null || movieInfo == null) {
            showErrorToast(R.string.message_failing_to_get_movie_detail);
            return;
        }

        // extract year from release date, recall release date in format yyyy-mm-dd
        String releaseYear = movieInfo.getReleaseDate().trim().substring(0, 4);

        ((TextView) findViewById(R.id.detail_title)).setText(movieInfo.getOriginalTitle());
        ((TextView) findViewById(R.id.detail_release_date)).setText(releaseYear);
        ((TextView) findViewById(R.id.detail_user_rating)).setText(movieInfo.getUserRating() + "/10");
        ((TextView) findViewById(R.id.detail_plot_synopsis)).setText(movieInfo.getPlotSynopsis());

        ImageView posterView = findViewById(R.id.detail_iv_poster);

        String posterUrl = movieInfo.getPosterUrl();

        if (posterUrl == null || posterUrl.length() == 0)
            throw new RuntimeException("poster url can't be null or empty");

        Picasso.with(this)
                .load(posterUrl)
                .into(posterView);

        new FetchExtraMovieInfo().execute(movieInfo.getId());

    }

    private void populateUIWithExtraInfo() {

        ((TextView) findViewById(R.id.detail_running_time)).setText(mExtraMovieInfo.getRunningTime() + "min");

        String trailer1Url = mExtraMovieInfo.getTrailer1Url();
        String trailer2Url = mExtraMovieInfo.getTrailer2Url();
        String reviewsInHtml = mExtraMovieInfo.getReviewsInHtml();

        if (trailer1Url != null && trailer1Url.length() > 0) {
            findViewById(R.id.detail_label_trailers).setVisibility(View.VISIBLE);
            findViewById(R.id.detail_button_trailer1).setVisibility(View.VISIBLE);
            findViewById(R.id.detail_horizontal_rule_after_trailer1).setVisibility(View.VISIBLE);
        }

        if (trailer2Url != null && trailer2Url.length() > 0) {
            findViewById(R.id.detail_button_trailer2).setVisibility(View.VISIBLE);
            findViewById(R.id.detail_horizontal_rule_after_trailer2).setVisibility(View.VISIBLE);
        }

        if (reviewsInHtml != null && reviewsInHtml.length() > 0) {
            findViewById(R.id.detail_label_reviews).setVisibility(View.VISIBLE);
            TextView reviewsTv = findViewById(R.id.detail_reviews);
            reviewsTv.setVisibility(View.VISIBLE);
            reviewsTv.setText(HelperUtils.fromHtml(reviewsInHtml));
        }
    }

    public void handleTrailerButtonClick(View view) {
        switch (view.getId()) {
            case R.id.detail_button_trailer1:
                Intent trailer1Intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExtraMovieInfo.getTrailer1Url()));
                if (trailer1Intent.resolveActivity(getPackageManager()) != null)
                    startActivity(trailer1Intent);
                else
                    showErrorToast(R.string.message_cannot_play_video);
                break;
            case R.id.detail_button_trailer2:
                Intent trailer2Intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mExtraMovieInfo.getTrailer2Url()));
                if (trailer2Intent.resolveActivity(getPackageManager()) != null)
                    startActivity(trailer2Intent);
                else
                    showErrorToast(R.string.message_cannot_play_video);
        }

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

    private void showErrorToast(int stringResId) {
        Toast.makeText(this, stringResId, Toast.LENGTH_SHORT).show();
    }

}
