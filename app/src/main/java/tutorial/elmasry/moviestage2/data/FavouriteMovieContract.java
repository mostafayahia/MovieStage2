package tutorial.elmasry.moviestage2.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by yahia on 2/9/18.
 */

public class FavouriteMovieContract {

    public static final String AUTHORITY = "tutorial.elmasry.moviestage2";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVOURITES = "favourites";

    public static final class FavouriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVOURITES).build();

        public static final String TABLE_NAME = "favouriteMovies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER_URL = "posterUrl";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_RUNNING_TIME = "runningTime";
        public static final String COLUMN_USER_RATING = "userRating";
        public static final String COLUMN_PLOT_SYNOPSIS = "plotSynopsis";
        public static final String COLUMN_TRAILER1_URL = "trailer1Url";
        public static final String COLUMN_TRAILER2_URL = "trailer2Url";
        public static final String COLUMN_REVIEWS_IN_HTML = "reviewsInHtml";


    }


}
