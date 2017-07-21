package com.hugiell.popularmoviesapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;
import com.hugiell.popularmoviesapp.data.MovieDataContract;
import com.squareup.picasso.Picasso;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    Menu mMenu;
    MenuItem mShareActionMenuItem;
    private static String displayedMovieId = null;
    private static final int SHARE_ACTION_MENU_ITEM = 0;

    ShareActionProvider mShareActionProvider;
    String mVideoToShare;

    ViewGroup mVideoSlotContainer;
    ViewGroup mReviewSlotContainer;
    TextView mTitleView;
    ImageView mPosterView;
    TextView mReleaseDateView;
    TextView mRuntimeView;
    TextView mRatingView;
    TextView mOverviewView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        mShareActionMenuItem = menu.add(Menu.NONE, SHARE_ACTION_MENU_ITEM, Menu.NONE, R.string.share_video);
        setupShareVideoAction(mShareActionMenuItem, mVideoToShare);
    }

    @Override
    public void onStop() {
        // When fragment is closed remove the "share action" menu item
        mMenu.removeItem(SHARE_ACTION_MENU_ITEM);
        super.onStop();
    }

    void setupShareVideoAction(MenuItem menuItem, String videoAddress) {
        if (menuItem != null && videoAddress != null) {
            // Create share intent
            Uri videoUri = Uri.parse(videoAddress);
            Intent shareIntent = new Intent(Intent.ACTION_VIEW);
            shareIntent.setData(videoUri);
            // set share intent to ShareActionProvider
            ShareActionProvider shareActionProvider = new ShareActionProvider(getActivity());
            shareActionProvider.setShareIntent(shareIntent);
            menuItem.setActionProvider(shareActionProvider);
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Retrieve arguments sent by MainActivity with position of the movie selected in MainFragment
        Bundle receivedArgs = getArguments();
        displayedMovieId = receivedArgs.getString(MainActivity.MOVIE_ID_KEY);
        // Create CursorLoaders to get a cursor from content provider so that when the content provide's data
        // for that cursor changes cursor is also updated. We only need loaders when sorting order is set
        // to "popular movies" or "highest-rated movies"
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String chosenSortByPref = sharedPrefs.getString(getActivity().getString(R.string.settings_sortBy_key), "1");
        int chosenSortByPrefInt = Integer.valueOf(chosenSortByPref);
        switch (chosenSortByPrefInt) {
            case (1):
            case (2): {
                getLoaderManager().initLoader(MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS, null, this);
                getLoaderManager().initLoader(MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS_VIDEOS, null, this);
                getLoaderManager().initLoader(MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS_REVIEWS, null, this);
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View detailFragmentLayout = inflater.inflate(R.layout.fragment_detail, container, false);
        return detailFragmentLayout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Assign DetailFragment views to member variables to simplify its use across the DetailFragment
        Activity parentActivity = getActivity();
        mVideoSlotContainer = (ViewGroup) getActivity().findViewById(R.id.video_slots_container);
        mReviewSlotContainer = (ViewGroup) getActivity().findViewById(R.id.review_slots_container);
        mTitleView = ((TextView) parentActivity.findViewById(R.id.movieDetail_title));
        mPosterView = ((ImageView) parentActivity.findViewById(R.id.movieDetail_thumb));
        mReleaseDateView = ((TextView) parentActivity.findViewById(R.id.movieDetail_releaseDate));
        mRuntimeView = ((TextView) parentActivity.findViewById(R.id.movieDetail_runtime));
        mRatingView = ((TextView) parentActivity.findViewById(R.id.movieDetail_rating));
        mOverviewView = ((TextView) parentActivity.findViewById(R.id.movieDetail_overview));

        // Make sure "Mark as favorite" button saves the movie to the list of favorite movies
        Button favoriteButton = (Button) getActivity().findViewById(R.id.markAsFavorite_button);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri favoriteMovieUri = getActivity().getContentResolver().insert(MovieDataContract.FavoriteMovies.buildFavoriteMoviesUri(displayedMovieId), null);
                if (favoriteMovieUri != null) {
                    Toast.makeText(getActivity(), "Successfully added to favorites!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Movie already in favorites!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String chosenSortByPref = sharedPrefs.getString(getActivity().getString(R.string.settings_sortBy_key), "1");
        int chosenSortByPrefInt = Integer.valueOf(chosenSortByPref);
        switch (chosenSortByPrefInt) {
            case (1):
            case (2): {
                // In the case of popular and highest-razed movies we need to obtain data from the internet
                // In the case of favorite movies we obtain data from the database (since offline browsing should be enabled)
                // All CursorLoaders depending on that query will be notified and updated
                Uri movieDetailsUri = MovieDataContract.MovieDetails.buildMovieDetailsUri(displayedMovieId);
                getActivity().getContentResolver().update(movieDetailsUri,
                        null,
                        null,
                        null);

                Uri movieDetailsVideosUri = MovieDataContract.MovieDetails.buildMovieDetailsVideosUri(displayedMovieId);
                getActivity().getContentResolver().update(movieDetailsVideosUri,
                        null,
                        null,
                        null);

                Uri movieDetailsReviewsUri = MovieDataContract.MovieDetails.buildMovieDetailsReviewsUri(displayedMovieId);
                getActivity().getContentResolver().update(movieDetailsReviewsUri,
                        null,
                        null,
                        null);
                break;
            }
            case (3): {
                // Query the content provider for details about favorite movie
                // Since favorite movie data is saved inside the database, and therefore retrieval will be fast
                // we don't need to use CursorLoader

                // Get and process movie details
                Uri favoriteMovieDetailsUri = MovieDataContract.FavoriteMovies.buildFavoriteMovieDetailsUri(displayedMovieId);
                Cursor favoriteMovieDetailsCursor = getActivity().getContentResolver().query(
                        favoriteMovieDetailsUri,
                        null,
                        null,
                        null,
                        null,
                        null);
                favoriteMovieDetailsCursor.moveToFirst();
                String s = favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_TITLE));
                mTitleView.setText(favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_TITLE)));
                // If device has internet connectivity load images from the internet, otherwise load them from posters folder on disk
                Picasso picasso = Picasso.with(getActivity());
                String posterFilename = favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_POSTER));
                String posterPath = "http://image.tmdb.org/t/p/" + MovieDataContract.POSTER_SIZE_500 + posterFilename;
                if (!Utility.isConnected(getActivity())) {
                    posterPath = "file://" + MovieDataContract.FILEPATH_POSTERS + posterFilename;
                }
                picasso.load(posterPath).into(mPosterView);
                mReleaseDateView.setText(favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_RELEASE_DATE)));
                mRuntimeView.setText(favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_RUNTIME)));
                mRatingView.setText(favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_RATING)) + "/10");
                mOverviewView.setText(favoriteMovieDetailsCursor.getString(favoriteMovieDetailsCursor.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_OVERVIEW)));

                // Get and process movie videos
                Uri favoriteMovieDetailsVideosUri = MovieDataContract.Videos.buildFavoriteMovieVideosUri(displayedMovieId);
                Cursor favoriteMovieVideosCursor = getActivity().getContentResolver().query(
                        favoriteMovieDetailsVideosUri,
                        null,
                        null,
                        null,
                        null,
                        null);
                mVideoSlotContainer.removeAllViews();
                // Only show trailers header before first video
                if (favoriteMovieVideosCursor.moveToFirst()) {
                    boolean firstVideo = true;
                    do {
                        if (firstVideo) {
                            firstVideo = false;
                            // Add "Trailers" header
                            TextView trailersHeader = new TextView(getActivity());
                            trailersHeader.setText(R.string.trailers);
                            trailersHeader.setTextSize(24);
                            trailersHeader.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            params.setMargins(20, 20, 20, 20);
                            trailersHeader.setLayoutParams(params);
                            mVideoSlotContainer.addView(trailersHeader);
                        }
                        View videoSlot = LayoutInflater.from(getActivity()).inflate(R.layout.video_slot, null, false);
                        TextView videoNameView = (TextView) videoSlot.findViewById(R.id.listItem_videoName);
                        videoNameView.setText(favoriteMovieVideosCursor.getString(favoriteMovieVideosCursor.getColumnIndex(MovieDataContract.Videos.COLUMN_NAME_NAME)));
                        mVideoSlotContainer.addView(videoSlot);
                        final String videoKey = favoriteMovieVideosCursor.getString(favoriteMovieVideosCursor.getColumnIndex(MovieDataContract.Videos.COLUMN_NAME_KEY)); // declared final to be able to access it from button listener
                        final String videoSite = favoriteMovieVideosCursor.getString(favoriteMovieVideosCursor.getColumnIndex(MovieDataContract.Videos.COLUMN_NAME_SITE)); // declared final to be able to access it from button listener
                        mVideoToShare = "https://www.youtube.com/watch?v=" + videoKey;
                        setupShareVideoAction(mShareActionMenuItem, mVideoToShare);
                        ImageButton playButton = (ImageButton) videoSlot.findViewById(R.id.play_button);
                        playButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MediaProcessor.playMedia(getActivity(), videoKey, videoSite);
                            }
                        });
                    } while (favoriteMovieVideosCursor.moveToNext());
                }

                // Get and process movie reviews
                Uri favoriteMovieDetailsReviewsUri = MovieDataContract.UserReviews.buildFavoriteMovieUserReviewsUri(displayedMovieId);
                Cursor favoriteMovieUserReviewsCursor = getActivity().getContentResolver().query(
                        favoriteMovieDetailsReviewsUri,
                        null,
                        null,
                        null,
                        null,
                        null);
                mReviewSlotContainer.removeAllViews();
                // Only show "User reviews" header before first slot
                if (favoriteMovieUserReviewsCursor.moveToFirst()) {
                    boolean firstReview = true;
                    do {
                        if (firstReview) {
                            firstReview = false;
                            // Add "User reviews" header
                            TextView reviewHeader = new TextView(getActivity());
                            reviewHeader.setText(R.string.user_reviews);
                            reviewHeader.setTextSize(24);
                            reviewHeader.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            params.setMargins(20, 20, 20, 20);
                            reviewHeader.setLayoutParams(params);
                            mReviewSlotContainer.addView(reviewHeader);
                        }
                        View reviewSlot = LayoutInflater.from(getActivity()).inflate(R.layout.review_slot, null, false);
                        TextView reviewAuthorView = (TextView) reviewSlot.findViewById(R.id.listItem_reviewAuthor);
                        TextView reviewContentView = (TextView) reviewSlot.findViewById(R.id.listItem_reviewContent);
                        reviewAuthorView.setText(getResources().getString(R.string.review_by) + favoriteMovieUserReviewsCursor.getString(favoriteMovieUserReviewsCursor.getColumnIndex(MovieDataContract.UserReviews.COLUMN_NAME_AUTHOR)));
                        reviewContentView.setText(favoriteMovieUserReviewsCursor.getString(favoriteMovieUserReviewsCursor.getColumnIndex(MovieDataContract.UserReviews.COLUMN_NAME_CONTENT)));
                        mReviewSlotContainer.addView(reviewSlot);
                    } while (favoriteMovieUserReviewsCursor.moveToNext());
                }
            }
        }
    }


    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS: {
                Uri movieDetailsUri = MovieDataContract.MovieDetails.buildMovieDetailsUri(displayedMovieId);
                CursorLoader loader = new CursorLoader(
                        getActivity(),
                        movieDetailsUri,
                        null,
                        null,
                        null,
                        null);
                return loader;
            }
            case MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS_VIDEOS: {
                Uri movieDetailsUri = MovieDataContract.MovieDetails.buildMovieDetailsVideosUri(displayedMovieId);
                CursorLoader loader = new CursorLoader(
                        getActivity(),
                        movieDetailsUri,
                        null,
                        null,
                        null,
                        null);
                return loader;
            }
            case MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS_REVIEWS: {
                Uri movieDetailsUri = MovieDataContract.MovieDetails.buildMovieDetailsReviewsUri(displayedMovieId);
                CursorLoader loader = new CursorLoader(
                        getActivity(),
                        movieDetailsUri,
                        null,
                        null,
                        null,
                        null);
                return loader;
            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        // Try getting out Movie object with getExtras, otherwise you need to convert Movies to primitive types
        // Populate the detail view's views with the results from the cursor
        Activity parentActivity = getActivity();
        // Try/catch statement prevents application to crash if cursor values are still null
        if ((data != null) && (data.getCount() > 0)) {
            data.moveToFirst();
            if (!data.isNull(1)) { // if second column is not null, the cursor is filled with data
                switch (loader.getId()) {
                    case MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS: {
                        data.moveToFirst();
                        mTitleView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_TITLE)));
                        // http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
                        String posterUri = "http://image.tmdb.org/t/p/w500/" + data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_POSTER));
                        Picasso.with(getActivity()).load(posterUri).into(mPosterView);
                        mReleaseDateView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_RELEASE_DATE)));
                        mRuntimeView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_RUNTIME)));
                        mRatingView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_RATING)) + "/10");
                        mOverviewView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_OVERVIEW)));
                        break;
                    }
                    case MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS_VIDEOS: {
                        // Iterate throug cursor and add its items to the list of trailers
//                        ViewGroup videoSlotContainer = (ViewGroup)getActivity().findViewById(R.id.video_slots_container);
                        mVideoSlotContainer.removeAllViews();
                        // Only show trailers header before first video
                        data.moveToFirst();
                        boolean firstVideo = true;
                        do {
                            if (firstVideo) {
                                firstVideo = false;
                                // Add "Trailers" header
                                TextView trailersHeader = new TextView(getActivity());
                                trailersHeader.setText(R.string.trailers);
                                trailersHeader.setTextSize(24);
                                trailersHeader.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                params.setMargins(20, 20, 20, 20);
                                trailersHeader.setLayoutParams(params);
                                mVideoSlotContainer.addView(trailersHeader);
                            }
                            View videoSlot = LayoutInflater.from(getActivity()).inflate(R.layout.video_slot, null, false);
                            TextView videoNameView = (TextView) videoSlot.findViewById(R.id.listItem_videoName);
                            videoNameView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_NAME)));
                            mVideoSlotContainer.addView(videoSlot);
                            final String videoKey = data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_KEY)); // declared final to be able to access it from button listener
                            final String videoSite = data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_SITE)); // declared final to be able to access it from button listener
                            mVideoToShare = "https://www.youtube.com/watch?v=" + videoKey;
                            setupShareVideoAction(mShareActionMenuItem, mVideoToShare);
                            ImageButton playButton = (ImageButton) videoSlot.findViewById(R.id.play_button);
                            playButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MediaProcessor.playMedia(getActivity(), videoKey, videoSite);
                                }
                            });
                        } while (data.moveToNext());
                        break;
                    }
                    case MovieDataContract.CURSOR_LOADER_MOVIE_DETAILS_REVIEWS: {
                        // Iterate throug cursor and add its items to the list of trailers
//                        ViewGroup reviewSlotContainer = (ViewGroup)getActivity().findViewById(R.id.review_slots_container);
                        mReviewSlotContainer.removeAllViews();
                        // Only show "User reviews" header before first slot
                        data.moveToFirst();
                        boolean firstReview = true;
                        do {
                            if (firstReview) {
                                firstReview = false;
                                // Add "User reviews" header
                                TextView reviewHeader = new TextView(getActivity());
                                reviewHeader.setText(R.string.user_reviews);
                                reviewHeader.setTextSize(24);
                                reviewHeader.setTextColor(ContextCompat.getColor(getActivity(), R.color.lightGrey));
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                params.setMargins(20, 20, 20, 20);
                                reviewHeader.setLayoutParams(params);
                                mReviewSlotContainer.addView(reviewHeader);
                            }
                            View reviewSlot = LayoutInflater.from(getActivity()).inflate(R.layout.review_slot, null, false);
                            TextView reviewAuthorView = (TextView) reviewSlot.findViewById(R.id.listItem_reviewAuthor);
                            TextView reviewContentView = (TextView) reviewSlot.findViewById(R.id.listItem_reviewContent);
                            reviewAuthorView.setText(getResources().getString(R.string.review_by) + data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_REVIEW_AUTHOR)));
                            reviewContentView.setText(data.getString(data.getColumnIndex(MovieDataContract.MovieDetails.COLUMN_NAME_REVIEW_CONTENT)));
                            mReviewSlotContainer.addView(reviewSlot);
                        } while (data.moveToNext());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
    }

}