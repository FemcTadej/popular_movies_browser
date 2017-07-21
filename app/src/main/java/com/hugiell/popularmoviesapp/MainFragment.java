package com.hugiell.popularmoviesapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hugiell.popularmoviesapp.data.MovieDataContract;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    TopMoviesToGridViewAdapter topMoviesToGridViewAdapter;
    // Parent activity's implementation of the OnMoviePosterSelectedListener interface
    // onMoviePosterSelected of the MainActivity is called when movie poster is selected in the MainFragment
    OnMoviePosterSelectedListener mOnMoviePosterSelectedListener;
    Cursor moviesCursor; //Holds cursor for "popular movies", "highest rated" or "favorite" movies"

    /**
     * Implement this interface to receive event notifications from MainFragment
     */
    public interface OnMoviePosterSelectedListener {
        void onMoviePosterSelected(String movieId);
    }

    public MainFragment() {
        // Required empty public constructor
    }

    public TopMoviesToGridViewAdapter getTopMoviesToGridViewAdapter() {
        return topMoviesToGridViewAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Make sure that activity to which this fragment is attached to is implementing OnMoviePosterSelectedListener
        try {
            mOnMoviePosterSelectedListener = (OnMoviePosterSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnMoviePosterSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // If device is not connected to the internet show the favorites
        LinearLayout networkConnectionMsgContainer = (LinearLayout) getActivity().findViewById(R.id.networkConnectionMsg_container);
        LinearLayout connectionMsg = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.msg_network_connection, null);
        if (!Utility.isConnected(getActivity()) && !Utility.isSortListByFavorites(getActivity())) {
            // Add message only if there isn't one already
            if (networkConnectionMsgContainer.getChildCount() < 1) {
                networkConnectionMsgContainer.addView(connectionMsg);
                ((Button) connectionMsg.findViewById(R.id.retry_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Refresh the fragment
                        MainFragment.this.onResume();
                    }
                });
            }
        } else {
            networkConnectionMsgContainer.removeAllViews();
        }

        // When MainFragment shows up, send update query to the content provider
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String chosenSortByPref = sharedPrefs.getString(getActivity().getString(R.string.settings_sortBy_key), "1");
        int chosenSortByPrefInt = Integer.valueOf(chosenSortByPref);
        // If internet connection is active and sort order is "popular movies" or highest rated" movies show message
        // If sort order is "favorite movies" then internet connection is not needed
        if (!Utility.isConnected(getActivity())) {
            if ((chosenSortByPrefInt == 1) || (chosenSortByPrefInt == 2)) {
                Toast.makeText(getActivity(), R.string.no_network_connection, Toast.LENGTH_LONG).show();
            }
        } else {
            switch (chosenSortByPrefInt) {
                case 1: {
                    getActivity().getContentResolver().update(
                            MovieDataContract.PopularMovies.POPULAR_MOVIES_URI,
                            null,
                            null,
                            null);
                    break;
                }
                case 2: {
                    getActivity().getContentResolver().update(
                            MovieDataContract.HighestRatedMovies.HIGHEST_RATED_MOVIES_URI,
                            null,
                            null,
                            null);
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // create ImageAdapter
        topMoviesToGridViewAdapter = new TopMoviesToGridViewAdapter(getActivity(), moviesCursor, true);
        // instantiate and attach custom adapter which provides data for the main screen grid view
        GridView movieThumbsGridView = (GridView) getActivity().findViewById(R.id.gridView_postersGrid);
        movieThumbsGridView.setAdapter(topMoviesToGridViewAdapter);
        // register and implement onClick listener
        movieThumbsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Item click should only be enabled if network connection is enabled or "sort by favorites" is
                // selected in preferences (since favorites should be accessible even without network connection)
                if (Utility.isConnected(getActivity()) || Utility.isSortListByFavorites(getActivity())) {
                    // Notify MainActivity that poster was selected in the MainFragment
                    moviesCursor.moveToPosition(position);
                    mOnMoviePosterSelectedListener.onMoviePosterSelected(moviesCursor.getString(moviesCursor.getColumnIndex(MovieDataContract.PopularMovies.COLUMN_NAME_MOVIE_ID)));
                } else {
                    Toast.makeText(getActivity(), R.string.no_network_connection, Toast.LENGTH_LONG).show();
                }
            }
        });

        // read preferences to obtain a preferred sorting list
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String chosenSortByPref = sharedPrefs.getString(getActivity().getString(R.string.settings_sortBy_key), "1");
        int chosenSortByPrefInt = Integer.valueOf(chosenSortByPref);
        switch (chosenSortByPrefInt) {
            case 1: {
                getLoaderManager().initLoader(MovieDataContract.CURSOR_LOADER_POPULAR_MOVIES, null, this);
                break;
            }
            case 2: {
                getLoaderManager().initLoader(MovieDataContract.CURSOR_LOADER_HIGHEST_RATED_MOVIES, null, this);
                break;
            }
            case 3: {
                getLoaderManager().initLoader(MovieDataContract.CURSOR_LOADER_FAVORITE_MOVIES, null, this);
                break;
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MovieDataContract.CURSOR_LOADER_POPULAR_MOVIES: {
                CursorLoader loader = new CursorLoader(getActivity(),
                        MovieDataContract.PopularMovies.POPULAR_MOVIES_URI,
                        null,
                        null,
                        null,
                        null);
                return loader;
            }
            case MovieDataContract.CURSOR_LOADER_HIGHEST_RATED_MOVIES: {
                CursorLoader loader = new CursorLoader(getActivity(),
                        MovieDataContract.HighestRatedMovies.HIGHEST_RATED_MOVIES_URI,
                        null,
                        null,
                        null,
                        null);
                return loader;
            }
            case MovieDataContract.CURSOR_LOADER_FAVORITE_MOVIES: {
                CursorLoader loader = new CursorLoader(getActivity(),
                        MovieDataContract.FavoriteMovies.FAVORITE_MOVIES_URI,
                        null,
                        null,
                        null,
                        null);
                return loader;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // This will be called every time data in content provider changes for URI bound to this CursorLoader
        // No matter which loader is finished always refresh adapter with its data, since it doesn't matter if they are
        // most popular, highest-rated or favorite movies
        moviesCursor = data; // update the list of the top movies so that it can be used elsewhere
        topMoviesToGridViewAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        topMoviesToGridViewAdapter.changeCursor(null);
    }

}