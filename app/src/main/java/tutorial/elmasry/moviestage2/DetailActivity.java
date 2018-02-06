package tutorial.elmasry.moviestage2;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;

import tutorial.elmasry.moviestage2.model.ExtraMovieInfo;
import tutorial.elmasry.moviestage2.model.MovieInfo;
import tutorial.elmasry.moviestage2.utilities.NetworkUtils;
import tutorial.elmasry.moviestage2.utilities.TheMovieDBJsonUtils;

public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static final String EXTRA_MOVIE_INFO = "movie-info";

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
            showErrorToast();
            return;
        }

        mExtraMovieInfo = new ExtraMovieInfo();

        // extract year from release date, recall release date in format yyyy-mm-dd
        String releaseYear = movieInfo.getReleaseDate().trim().substring(0, 4);

        ((TextView) findViewById(R.id.detail_title)).setText(movieInfo.getOriginalTitle());
        ((TextView) findViewById(R.id.detail_release_date)).setText(releaseYear);
        ((TextView) findViewById(R.id.detail_user_rating)).setText(movieInfo.getUserRating()+"/10");
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

    private void showErrorToast() {
        Toast.makeText(this, R.string.message_failing_to_get_movie_detail, Toast.LENGTH_SHORT).show();
    }

    private class FetchExtraMovieInfo extends AsyncTask<Integer, Void, String[]> {

        private final int[] mUrlForArray = { NetworkUtils.URL_FOR_MOVIE_EXTRA_INFO,
                NetworkUtils.URL_FOR_MOVIE_REVIEWS,
                NetworkUtils.URL_FOR_MOVIE_TRAILERS
        };

        @Override
        protected String[] doInBackground(Integer... integers) {

            int movieId = integers[0];
            String[] jsonResponseArray = new String[mUrlForArray.length];

            try {

                for (int i = 0; i < jsonResponseArray.length; i++)
                    jsonResponseArray[i] = NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrlWithId(movieId, mUrlForArray[i]));

                return jsonResponseArray;

            } catch (IOException e) {

                Log.e(LOG_TAG, "error in getting json response after building url with id");
                e.printStackTrace();
                return null;

            }

        }

        private void populateUIWithExtraInfo() {
            ((TextView) findViewById(R.id.detail_running_time)).setText(mExtraMovieInfo.getRunningTime()+"min");
        }

        @Override
        protected void onPostExecute(String[] jsonResponseArray) {

            if (null == jsonResponseArray) return;

            for (int i = 0; i < jsonResponseArray.length; i++) {
                int urlFor = mUrlForArray[i];
                switch (urlFor) {
                    case NetworkUtils.URL_FOR_MOVIE_EXTRA_INFO:
                        try {
                            int runningTime = TheMovieDBJsonUtils.getMovieRunningTimeFromJson(jsonResponseArray[i]);
                            mExtraMovieInfo.setRunningTime(runningTime);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }

            populateUIWithExtraInfo();

        }
    }


}
