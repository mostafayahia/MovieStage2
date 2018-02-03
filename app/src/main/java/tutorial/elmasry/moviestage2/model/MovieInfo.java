package tutorial.elmasry.moviestage2.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yahia on 1/29/18.
 */

public class MovieInfo implements Parcelable {

    private int id;
    private String originalTitle;
    private String posterUrl;
    private String plotSynopsis;
    private double userRating;
    private String releaseDate;


    public MovieInfo() {

    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private MovieInfo(Parcel parcel) {

        this.id = parcel.readInt();
        this.originalTitle = parcel.readString();
        this.posterUrl = parcel.readString();
        this.plotSynopsis = parcel.readString();
        this.userRating = parcel.readDouble();
        this.releaseDate = parcel.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(id);
        dest.writeString(originalTitle);
        dest.writeString(posterUrl);
        dest.writeString(plotSynopsis);
        dest.writeDouble(userRating);
        dest.writeString(releaseDate);

    }

    public static final Parcelable.Creator CREATOR = new Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel source) {
            return new MovieInfo(source);
        }

        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[0];
        }
    };
}
