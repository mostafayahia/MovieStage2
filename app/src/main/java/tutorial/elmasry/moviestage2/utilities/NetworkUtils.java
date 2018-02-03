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
