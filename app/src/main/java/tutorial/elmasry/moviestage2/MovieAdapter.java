package tutorial.elmasry.moviestage2;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import tutorial.elmasry.moviestage2.model.MovieInfo;

/**
 * Created by yahia on 1/30/18.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private MovieInfo[] mMovieInfoArray;
    private final Context mContext;
    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler {
        void clickHandler(MovieInfo movieInfo);
    }

    public MovieAdapter(Context context, MovieAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.grid_list_item, parent, false);
        return new MovieAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieAdapterViewHolder holder, int position) {

        String posterUrl = mMovieInfoArray[position].getPosterUrl();

        if (posterUrl == null || posterUrl.length() == 0)
            throw new RuntimeException("poster url can't be null or empty");

        Picasso.with(mContext)
                .load(posterUrl)
                .into(holder.mPosterView);
    }

    @Override
    public int getItemCount() {
        if (mMovieInfoArray == null) return 0;
        else                         return mMovieInfoArray.length;
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mPosterView;

        public MovieAdapterViewHolder(View itemView) {
            super(itemView);
            mPosterView = itemView.findViewById(R.id.detail_iv_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mClickHandler.clickHandler(mMovieInfoArray[getAdapterPosition()]);
        }
    }

    public void setMovieInfoArray(MovieInfo[] mMovieInfoArray) {
        this.mMovieInfoArray = mMovieInfoArray;
        notifyDataSetChanged();
    }
}
