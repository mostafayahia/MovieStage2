package tutorial.elmasry.moviestage2.utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tutorial.elmasry.moviestage2.model.BasicMovieInfo;

/**
 * Created by yahia on 1/29/18.
 */

public class TheMovieDBJsonUtils {



    public static BasicMovieInfo[] getMovieInfoArrayFromJson(String jsonResponseString) throws JSONException {

        final String TMDB_RESULTS = "results";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_USER_RATING = "vote_average";
        final String TMDB_ID = "id";
        final String TMDB_PLOT_SYNOPSIS = "overview";
        final String TMDB_RELEASE_DATE = "release_date";


        JSONArray resultsJsonArray = new JSONObject(jsonResponseString).getJSONArray(TMDB_RESULTS);

        BasicMovieInfo[] basicMovieInfoArray = new BasicMovieInfo[resultsJsonArray.length()];

        for (int i = 0; i < resultsJsonArray.length(); i++) {

            BasicMovieInfo basicMovieInfo = new BasicMovieInfo();

            JSONObject movieInfoJson = (JSONObject) resultsJsonArray.get(i);

            basicMovieInfo.setId(movieInfoJson.getInt(TMDB_ID));
            basicMovieInfo.setOriginalTitle(movieInfoJson.getString(TMDB_ORIGINAL_TITLE));
            basicMovieInfo.setPlotSynopsis(movieInfoJson.getString(TMDB_PLOT_SYNOPSIS));
            basicMovieInfo.setPosterUrl("http://image.tmdb.org/t/p/w185"+movieInfoJson.getString(TMDB_POSTER_PATH));
            basicMovieInfo.setUserRating(movieInfoJson.getDouble(TMDB_USER_RATING));
            basicMovieInfo.setReleaseDate(movieInfoJson.getString(TMDB_RELEASE_DATE));

            basicMovieInfoArray[i] = basicMovieInfo;
        }

        return basicMovieInfoArray;

    }

    public static int getMovieRunningTimeFromJson(String jsonResponseString) throws JSONException {

        final String TMDB_RUNNING_TIME = "runtime";

        return new JSONObject(jsonResponseString).getInt(TMDB_RUNNING_TIME);

    }

    public static String getMovieReviewsInHtmlFromJson(String jsonResponseString) throws JSONException {

        final String TMDB_RESULTS = "results";
        final String TMDB_REVIEWER = "author";
        final String TMDB_REVIEW_CONTENT = "content";

        String allReviews = "";

        JSONArray jsonResultsArray = new JSONObject(jsonResponseString).getJSONArray(TMDB_RESULTS);

        for (int i = 0; i < jsonResultsArray.length(); i++) {
            JSONObject jsonReview = (JSONObject) jsonResultsArray.get(i);
            String reviewer = jsonReview.getString(TMDB_REVIEWER);
            String reviewContent = jsonReview.getString(TMDB_REVIEW_CONTENT);
            allReviews = allReviews.concat("<br><b>" + reviewer + ":</b><br><br>")
                    .concat(reviewContent)
                    .concat("<br><br><b>======================================</b><br>");
        }

        return allReviews.replace("\r\n", "<br>");
    }

    /**
     * @param jsonResponseString
     * @return null if there is no trailers for this movie
     * @throws JSONException
     */
    public static String[] getMovieTrailerUrlArrayFromJson(String jsonResponseString) throws JSONException {

        final String TMDB_RESULTS = "results";
        final String TMDB_VIDEO_KEY = "key";

        JSONArray jsonResultsArray = new JSONObject(jsonResponseString).getJSONArray(TMDB_RESULTS);

        final int TRAILERS_NUM = jsonResultsArray.length();

        if (TRAILERS_NUM == 0) return null;

        String[] movieTrailerUrlArray = new String[TRAILERS_NUM];

        for (int i = 0; i < TRAILERS_NUM; i++) {
            String videoKey = ((JSONObject) jsonResultsArray.get(i)).getString(TMDB_VIDEO_KEY);
            movieTrailerUrlArray[i] = "https://www.youtube.com/watch?v=" + videoKey;
        }

        return movieTrailerUrlArray;
    }

}
