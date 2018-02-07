package tutorial.elmasry.moviestage2.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yahia on 2/6/18.
 */

public final class ExtraMovieInfo implements Parcelable {

    private int movieId;
    private String trailer1Url;
    private String trailer2Url;
    private String reviewsInHtml;
    private int runningTime;

    public static final Creator<ExtraMovieInfo> CREATOR = new Creator<ExtraMovieInfo>() {
        @Override
        public ExtraMovieInfo createFromParcel(Parcel in) {
            return new ExtraMovieInfo(in);
        }

        @Override
        public ExtraMovieInfo[] newArray(int size) {
            return new ExtraMovieInfo[size];
        }
    };

    public ExtraMovieInfo() {

    }

    private ExtraMovieInfo(Parcel in) {
        movieId = in.readInt();
        trailer1Url = in.readString();
        trailer2Url = in.readString();
        reviewsInHtml = in.readString();
        runningTime = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(movieId);
        dest.writeString(trailer1Url);
        dest.writeString(trailer2Url);
        dest.writeString(reviewsInHtml);
        dest.writeInt(runningTime);
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTrailer2Url() {
        return trailer2Url;
    }

    public void setTrailer2Url(String trailer2Url) {
        this.trailer2Url = trailer2Url;
    }

    public String getReviewsInHtml() {
        return reviewsInHtml;
    }

    public void setReviewsInHtml(String reviewsInHtml) {
        this.reviewsInHtml = reviewsInHtml;
    }

    public int getRunningTime() {
        return runningTime;
    }

    public void setRunningTime(int runningTime) {
        this.runningTime = runningTime;
    }

    public String getTrailer1Url() {

        return trailer1Url;
    }

    public void setTrailer1Url(String trailer1Url) {
        this.trailer1Url = trailer1Url;
    }

}
