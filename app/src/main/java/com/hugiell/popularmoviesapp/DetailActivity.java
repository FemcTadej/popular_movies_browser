package com.hugiell.popularmoviesapp;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;

public class DetailActivity extends Activity {
    Menu mMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recieve the extra sent as part of the intent from MainActivity with the position of the selected movie poster
        String selectedMovieId = getIntent().getExtras().getString(MainActivity.MOVIE_ID_KEY);
        // Append DetailFragment to the detail_fragment_container in the DetailActivity and send position as an argument along with it
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(MainActivity.MOVIE_ID_KEY, selectedMovieId);
        detailFragment.setArguments(args);
        fragmentTransaction.replace(R.id.detail_fragment_container, detailFragment);
        fragmentTransaction.commit();
    }
}
