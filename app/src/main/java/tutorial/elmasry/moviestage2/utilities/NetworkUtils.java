package tutorial.elmasry.moviestage2.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


import tutorial.elmasry.moviestage2.BuildConfig;

/**
 * Created by yahia on 1/29/18.
 */

public final class NetworkUtils {

    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    public static final int SORT_POPULAR = 0;
    public static final int SORT_TOP_RATED = 1;

    public static final int URL_FOR_MOVIE_EXTRA_INFO = 100;
    public static final int URL_FOR_MOVIE_REVIEWS = 101;
    public static final int URL_FOR_MOVIE_TRAILERS = 102;

    private static final String API_KEY_PARAM = "api_key";


    /**
     * @param sortBy can be SORT_POPULAR or SORT_TOP_RATED
     * @return URL object will be used to make http request to get movies sorted by popularity or top rated
     */
    public static URL buildUrl(int sortBy) {

        final String urlString;

        switch (sortBy) {
            case SORT_POPULAR:
                urlString = "http://api.themoviedb.org/3/movie/popular";
                break;
            case SORT_TOP_RATED:
                urlString = "http://api.themoviedb.org/3/movie/top_rated";
                break;
            default:
                throw new IllegalArgumentException("undefined sortBy argument, given: " + sortBy);
        }

        return buildUrl(urlString);

    }

    /**
     * @param movieId the movie Id as defined in the movie db api
     * @param urlFor can be URL_FOR_MOVIE_EXTRA_INFO, URL_FOR_MOVIE_REVIEWS or URL_FOR_MOVIE_TRAILERS
     * @return URL object will be used to make http request to get movie trailers or reviews or extra info
     */
    public static URL buildUrlWithId(int movieId, int urlFor) {

        final String BASE_URL = "http://api.themoviedb.org/3/movie/" + movieId;

        final String urlString;

        switch (urlFor) {
            case URL_FOR_MOVIE_EXTRA_INFO:
                urlString = BASE_URL;
                break;
            case URL_FOR_MOVIE_REVIEWS:
                urlString = Uri.parse(BASE_URL).buildUpon()
                        .appendPath("reviews")
                        .build().toString();
                break;
            case URL_FOR_MOVIE_TRAILERS:
                urlString = Uri.parse(BASE_URL).buildUpon()
                        .appendPath("videos")
                        .build().toString();
                break;
            default:
                throw new IllegalArgumentException("undefined urlFor argument, given: " + urlFor);
        }

        return buildUrl(urlString);
    }

    private static URL buildUrl(String urlString) {
        Uri uri = Uri.parse(urlString).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVE_DB_API_KEY)
                .build();

        try {

            return new URL(uri.toString());

        } catch (MalformedURLException e) {

            Log.e(LOG_TAG, "can't construct movie url object");
            e.printStackTrace();
            return null;

        }
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        try {

            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }


}
