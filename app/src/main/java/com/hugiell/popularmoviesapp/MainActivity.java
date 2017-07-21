package com.hugiell.popularmoviesapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

import com.hugiell.popularmoviesapp.data.MovieDataContract;
import com.hugiell.popularmoviesapp.data.MovieDbHelper;

import java.io.File;

public class MainActivity extends Activity implements MainFragment.OnMoviePosterSelectedListener {

    boolean mDualPane = false;
    public static final String MOVIE_ID_KEY = "MOVIE_ID_KEY";

    Menu mMenu;
    ShareActionProvider mShareActionProvider;

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // create activity layout from xml layout file
        setContentView(R.layout.activity_main);

        MovieDbHelper dbHelper = new MovieDbHelper(this);
        dbHelper.getReadableDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if MainActivity is dual pane
        mDualPane = isDualPane();
    }

    /**
     * Check if the main activity layout is single or dual pane
     *
     * @return
     */
    public boolean isDualPane() {
        if (findViewById(R.id.detail_fragment_container) != null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        MovieDataServer.refreshSortedMoviesList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater settingsMenuInflater = getMenuInflater();
        settingsMenuInflater.inflate(R.menu.settings_menu, menu);
        menu.findItem(R.id.settingsMenu_settingsItem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // start SettingsActivity
                Intent settingsActivityIntent = new Intent(getApplicationContext(), SettingsActivity.class);
//                settingsActivityIntent.setComponent(;
                startActivity(settingsActivityIntent);
                return false;
            }
        });
        menu.findItem(R.id.settingsMenu_deleteFavoritesItem).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getContentResolver().delete(
                        MovieDataContract.FavoriteMovies.FAVORITE_MOVIES_URI,
                        null,
                        null
                );
                // Delete posters folder
                File posterDir = new File(MovieDataContract.FILEPATH_POSTERS);
                Utility.deleteFileeTree(posterDir);
                return true;
            }
        });
        return true;
    }

    @Override
    public void onMoviePosterSelected(String selectedMovieId) {
        // When user clicks poster show the details
        // Find the container into which to place DetailFragment
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isDualPane()) {
            DetailFragment detailFragment = new DetailFragment();
            Bundle args = new Bundle();
            args.putString(MOVIE_ID_KEY, selectedMovieId);
            detailFragment.setArguments(args);
            fragmentTransaction.replace(R.id.detail_fragment_container, detailFragment);
            fragmentTransaction.commit();
        } else { // single pane
            Intent detailActivityIntent = new Intent(this, DetailActivity.class);
            detailActivityIntent.putExtra(MOVIE_ID_KEY, selectedMovieId);
            startActivity(detailActivityIntent);
        }
    }
}