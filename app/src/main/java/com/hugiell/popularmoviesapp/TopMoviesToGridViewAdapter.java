package com.hugiell.popularmoviesapp;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.hugiell.popularmoviesapp.data.MovieDataContract;
import com.squareup.picasso.Picasso;

public class TopMoviesToGridViewAdapter extends CursorAdapter {

    public TopMoviesToGridViewAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public TopMoviesToGridViewAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView;
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);
        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView = (ImageView) view;
        String posterFilename = cursor.getString(cursor.getColumnIndex(MovieDataContract.PopularMovies.COLUMN_NAME_POSTER));
        // If device has internet connectivity load images from the internet, otherwise load them from posters folder on disk
        Picasso picasso = Picasso.with(context);
        String posterPath = "http://image.tmdb.org/t/p/" + MovieDataContract.POSTER_SIZE_500 + posterFilename;
        if (!Utility.isConnected(context)) {
            posterPath = "file://" + MovieDataContract.FILEPATH_POSTERS + posterFilename;
        }
        picasso.load(posterPath).into(imageView);
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        return super.runQueryOnBackgroundThread(constraint);
    }
}
