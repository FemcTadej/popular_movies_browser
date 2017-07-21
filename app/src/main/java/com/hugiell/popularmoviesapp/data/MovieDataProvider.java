package com.hugiell.popularmoviesapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.Log;

import com.hugiell.popularmoviesapp.Movie;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The main and only hub for accessing movie data
 * Using ContentProvider is the preferred way for accessing data
 * because it allows us to use CursorLoader and consequently
 * a reference to the cursor that refreshes whenever ContentProvider
 * data for that ContentProvider URI changes
 */
public class MovieDataProvider extends ContentProvider {

    private static final int POPULAR_MOVIES = 100;
    private static final int HIGHEST_RATED_MOVIES = 101;
    private static final int FAVORITE_MOVIES = 301;
    private static final int FAVORITE_MOVIE = 302;
    private static final int FAVORITE_MOVIE_VIDEOS = 303;
    private static final int FAVORITE_MOVIE_USER_REVIEWS = 304;
    private static final int MOVIE_DETAILS = 200;
    private static final int MOVIE_DETAILS_VIDEOS = 201;
    private static final int MOVIE_DETAILS_REVIEWS = 202;
    static File posterDir;
    Context mContext;
    MovieDataServer movieDataServer = new MovieDataServer();
    UriMatcher uriMatcher = buildUriMatcher();

    public MovieDataProvider() {
//        posterDir = new File(Environment.getDataDirectory() + "/data/" + MovieDataContract.GLOBAL_APP_IDENTIFIER + "/posters/");
        posterDir = new File(MovieDataContract.FILEPATH_POSTERS);
        posterDir.mkdir();
    }

    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // mapping com.hugiell.popularmoviesapp.provider/popular_movies -> 100
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.PopularMovies.TABLE_NAME, POPULAR_MOVIES);
        // mapping com.hugiell.popularmoviesapp.provider/highest_rated_movies -> 101
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.HighestRatedMovies.TABLE_NAME, HIGHEST_RATED_MOVIES);

        // mapping com.hugiell.popularmoviesapp.provider/details/188927 -> 200
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.MovieDetails.MOVIE_DETAILS + "/#", MOVIE_DETAILS);
        // mapping com.hugiell.popularmoviesapp.provider/details_videos/188927 -> 201
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.MovieDetails.MOVIE_DETAILS_VIDEOS + "/#", MOVIE_DETAILS_VIDEOS);
        // mapping com.hugiell.popularmoviesapp.provider/details_reviews/188927 -> 202
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.MovieDetails.MOVIE_DETAILS_REVIEWS + "/#", MOVIE_DETAILS_REVIEWS);

        // mapping com.hugiell.popularmoviesapp.provider/favorite_movies -> 301
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.FavoriteMovies.TABLE_NAME, FAVORITE_MOVIES);
        // mapping com.hugiell.popularmoviesapp.provider/favorite_movies/188927 -> 302
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.FavoriteMovies.TABLE_NAME + "/#", FAVORITE_MOVIE);
        // mapping com.hugiell.popularmoviesapp.provider/videos/188927 -> 303
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.Videos.TABLE_NAME + "/#", FAVORITE_MOVIE_VIDEOS);
        // mapping com.hugiell.popularmoviesapp.provider/user_reviews/188927 -> 304
        uriMatcher.addURI(MovieDataContract.CONTENT_AUTHORITY, MovieDataContract.UserReviews.TABLE_NAME + "/#", FAVORITE_MOVIE_USER_REVIEWS);
        return uriMatcher;
    }

    //target to save
    private static Target getTarget(final String url) {
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        File file = new File(MovieDataProvider.posterDir, url);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        };
        return target;
    }

    @Override
    public boolean onCreate() {
        this.mContext = getContext();
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        movieDataServer.movieDetails = new Movie();
        MovieDbHelper db = new MovieDbHelper(mContext);
        // Match incoming URI against the UriMatcher
        int code = uriMatcher.match(uri);
        switch (uriMatcher.match(uri)) {
            case FAVORITE_MOVIES: {
                // Delete tables: favorite_movies, user_reviews, videos
                int a = db.getWritableDatabase().delete(
                        MovieDataContract.FavoriteMovies.TABLE_NAME,
                        "1",
                        null
                );
                int b = db.getWritableDatabase().delete(
                        MovieDataContract.Videos.TABLE_NAME,
                        "1",
                        null
                );
                int c = db.getWritableDatabase().delete(
                        MovieDataContract.UserReviews.TABLE_NAME,
                        "1",
                        null
                );
                return 1;
            }
        }
        return 0;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String movieId = uri.getLastPathSegment();
        MovieDbHelper db = new MovieDbHelper(mContext);
        // Match incoming URI against the UriMatcher
        int code = uriMatcher.match(uri);
        switch (uriMatcher.match(uri)) {
            case POPULAR_MOVIES: {
                Cursor cursor = movieDataServer.getPopularMoviesAsCursor();
                cursor.setNotificationUri(getContext().getContentResolver(), MovieDataContract.PopularMovies.POPULAR_MOVIES_URI);
                return cursor;
            }
            case HIGHEST_RATED_MOVIES: {
                Cursor cursor = movieDataServer.getHighestRatedMoviesAsCursor();
                cursor.setNotificationUri(getContext().getContentResolver(), MovieDataContract.HighestRatedMovies.HIGHEST_RATED_MOVIES_URI);
                return cursor;
            }
            case MOVIE_DETAILS: {
                Cursor cursor = movieDataServer.getMovieDetailsAsCursor();
                cursor.setNotificationUri(getContext().getContentResolver(), MovieDataContract.MovieDetails.MOVIE_DETAILS_URI);
                return cursor;
            }
            case MOVIE_DETAILS_VIDEOS: {
                Cursor cursor = movieDataServer.getMovieDetailsVIdeosAsCursor();
                cursor.setNotificationUri(getContext().getContentResolver(), MovieDataContract.MovieDetails.MOVIE_DETAILS_VIDEOS_URI);
                return cursor;
            }
            case MOVIE_DETAILS_REVIEWS: {
                Cursor cursor = movieDataServer.getMovieDetailsReviewsAsCursor();
                cursor.setNotificationUri(getContext().getContentResolver(), MovieDataContract.MovieDetails.MOVIE_DETAILS_REVIEWS_URI);
                return cursor;
            }
            case FAVORITE_MOVIES: {
                Cursor cursor = db.getReadableDatabase().query(
                        MovieDataContract.FavoriteMovies.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                return cursor;
            }
            case FAVORITE_MOVIE: {
                Cursor cursor = db.getReadableDatabase().query(
                        MovieDataContract.FavoriteMovies.TABLE_NAME,
                        null,
                        MovieDataContract.FavoriteMovies.COLUMN_NAME_MOVIE_ID + " = ? ",
                        new String[]{movieId},
                        null,
                        null,
                        null
                );
                return cursor;
            }
            case FAVORITE_MOVIE_VIDEOS: {
                Cursor cursor = db.getReadableDatabase().query(
                        MovieDataContract.Videos.TABLE_NAME,
                        null,
                        MovieDataContract.FavoriteMovies.COLUMN_NAME_MOVIE_ID + " = ? ",
                        new String[]{movieId},
                        null,
                        null,
                        null
                );
                return cursor;
            }
            case FAVORITE_MOVIE_USER_REVIEWS: {
                Cursor cursor = db.getReadableDatabase().query(
                        MovieDataContract.UserReviews.TABLE_NAME,
                        null,
                        MovieDataContract.FavoriteMovies.COLUMN_NAME_MOVIE_ID + " = ? ",
                        new String[]{movieId},
                        null,
                        null,
                        null
                );
                return cursor;
            }
            default: {
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        movieDataServer.movieDetails = new Movie();
        String movieId = uri.getLastPathSegment();
        // Get access to the writable database
        int code = uriMatcher.match(uri);
        switch (uriMatcher.match(uri)) {
            case FAVORITE_MOVIE: {
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                // Check if the movie is already saved to favorites
                Cursor cursor = db.query(MovieDataContract.FavoriteMovies.TABLE_NAME,
                        null,
                        MovieDataContract.FavoriteMovies.COLUMN_NAME_MOVIE_ID + " = ?",
                        new String[]{movieId},
                        null,
                        null,
                        null);
                // Insert movie details for the movie with provided ID into the corresponding database
                // if the movie is not already in the database
                if (cursor.getCount() < 1) {
                    movieDataServer.insertMovieIntoFavorites(movieId);
                    return uri;
                }
            }
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        movieDataServer.movieDetails = new Movie();
        // Extract movie ID from the content provider's URI
        String movieId = uri.getLastPathSegment();
        switch (uriMatcher.match(uri)) {
            case POPULAR_MOVIES: {
                // This update doesn't need parameters since it only requeries popular movies from web API
                movieDataServer.updatePopularMoviesFromWeb();
                return movieDataServer.popularMovies.size();
            }
            case HIGHEST_RATED_MOVIES: {
                // This update doesn't need parameters since it only requeries highest-rated movies from web API
                movieDataServer.updateHighestRatedMoviesFromWeb();
                return movieDataServer.highestRatedMovies.size();
            }
            case MOVIE_DETAILS: {
                movieDataServer.updateMovieDetailsFromWeb(movieId);
                return 1;
            }
            case MOVIE_DETAILS_VIDEOS: {
                movieDataServer.updateMovieDetailsVideosFromWeb(movieId);
                return 1;
            }
            case MOVIE_DETAILS_REVIEWS: {
                movieDataServer.updateMovieDetailsReviewsFromWeb(movieId);
                return 1;
            }
        }
        return 0;
    }

    private class MovieDataServer {
        public final String API_KEY = "aef1f8d14e0518ba7f87ee50b62e2392";
        // Holds the latest list of popular movies and their data
        ArrayList<Movie> popularMovies = new ArrayList();
        // Holds the latest list of highest rated movies and their data
        ArrayList<Movie> highestRatedMovies = new ArrayList();
        // Holds the data for the currently displayed movie details
        Movie movieDetails = new Movie();
        private Movie[] sortedMoviesList; // list of popular/highest-rated movies
//        Movie movieDetails;

        // prevent user from instantiating MovieDataServer without parameters
        private MovieDataServer() {
        }

        public Movie getMovieDetails() {
            return movieDetails;
        }

        public Cursor getMovieDetailsAsCursor() {
            MatrixCursor matrixCursor = new MatrixCursor(MovieDataContract.MovieDetails.MOVIE_DETAILS_COLUMN_NAMES);
//        MovieDataContract.MovieDetails._ID,
//        MovieDataContract.MovieDetails.COLUMN_NAME_MOVIE_ID,
//        MovieDataContract.MovieDetails.COLUMN_NAME_TITLE,
//        MovieDataContract.MovieDetails.COLUMN_NAME_POSTER,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RELEASE_DATE,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RATING,
//        MovieDataContract.MovieDetails.COLUMN_NAME_OVERVIEW,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RUNTIME,
            if (movieDataServer.movieDetails != null) {
                Object[] movieDetails = new Object[]{
                        movieDataServer.movieDetails.getId(),
                        movieDataServer.movieDetails.getMovieId(),
                        movieDataServer.movieDetails.getTitle(),
                        movieDataServer.movieDetails.getPoster(),
                        movieDataServer.movieDetails.getReleaseDate(),
                        movieDataServer.movieDetails.getRating(),
                        movieDataServer.movieDetails.getOverview(),
                        movieDataServer.movieDetails.getRuntime(),
                };
                matrixCursor.addRow(movieDetails);
            }
            return matrixCursor;
        }

        public Cursor getMovieDetailsVIdeosAsCursor() {
            MatrixCursor matrixCursor = new MatrixCursor(MovieDataContract.MovieDetails.MOVIE_DETAILS_VIDEOS_COLUMN_NAMES);
            Object[] video = new Object[]{null, null, null, null, null, null};
            if (movieDataServer.movieDetails != null) {
                Movie.Video[] movieVideos = movieDataServer.movieDetails.getVideos();
//                MovieDataContract.MovieDetails._ID,
//                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_NAME,
//                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_TYPE,
//                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_KEY,
//                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_SITE,
//                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_SIZE
                if (movieVideos != null) {
                    for (int i = 0; i < movieVideos.length; i++) {
                        if (movieVideos[i] != null) {
                            video = new Object[]{
                                    movieVideos[i].getId(),
                                    movieVideos[i].getName(),
                                    movieVideos[i].getType(),
                                    movieVideos[i].getKey(),
                                    movieVideos[i].getSite(),
                                    movieVideos[i].getSize()
                            };
                            matrixCursor.addRow(video);
                        }
                    }
                }
            }
            return matrixCursor;
        }

        public Cursor getMovieDetailsReviewsAsCursor() {
            MatrixCursor matrixCursor = new MatrixCursor(MovieDataContract.MovieDetails.MOVIE_DETAILS_REVIEWS_COLUMN_NAMES);
            Object[] reviews = new Object[]{null, null, null};
            if (movieDataServer.movieDetails != null) {
                Movie.UserReview[] movieReviews = movieDataServer.movieDetails.getUserReviews();
//                MovieDataContract.MovieDetails._ID,
//                MovieDataContract.MovieDetails.COLUMN_NAME_REVIEW_AUTHOR,
//                MovieDataContract.MovieDetails.COLUMN_NAME_REVIEW_CONTENT
                if (movieReviews != null) {
                    for (int i = 0; i < movieReviews.length; i++) {
                        reviews = new Object[]{
                                movieReviews[i].getId(),
                                movieReviews[i].getAuthor(),
                                movieReviews[i].getContent()
                        };
                        matrixCursor.addRow(reviews);
                    }
                }
            }
            return matrixCursor;
        }

        public Cursor getPopularMoviesAsCursor() {
            MatrixCursor matrixCursor = new MatrixCursor(MovieDataContract.PopularMovies.COLUMN_NAMES);
//        MovieDataContract.MovieDetails._ID,
//        MovieDataContract.MovieDetails.COLUMN_NAME_MOVIE_ID,
//        MovieDataContract.MovieDetails.COLUMN_NAME_TITLE,
//        MovieDataContract.MovieDetails.COLUMN_NAME_POSTER,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RELEASE_DATE,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RATING,
//        MovieDataContract.MovieDetails.COLUMN_NAME_OVERVIEW,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RUNTIME,
//        MovieDataContract.MovieDetails.COLUMN_NAME_VIDEOSW,
//        MovieDataContract.MovieDetails.COLUMN_NAME_USER_REVIEWS
            if (movieDataServer.popularMovies.size() > 0) {
                for (int i = 0; i < popularMovies.size(); i++) {
                    Object[] popularMoviesRow = new Object[]{
                            movieDataServer.popularMovies.get(i).getId(),
                            movieDataServer.popularMovies.get(i).getMovieId(),
                            movieDataServer.popularMovies.get(i).getTitle(),
                            movieDataServer.popularMovies.get(i).getPoster(),
                            movieDataServer.popularMovies.get(i).getReleaseDate(),
                            movieDataServer.popularMovies.get(i).getRating(),
                            movieDataServer.popularMovies.get(i).getOverview()
                    };
                    matrixCursor.addRow(popularMoviesRow);
                }
            }
            return matrixCursor;
        }

        public Cursor getHighestRatedMoviesAsCursor() {
            MatrixCursor matrixCursor = new MatrixCursor(MovieDataContract.HighestRatedMovies.COLUMN_NAMES);
//        MovieDataContract.MovieDetails._ID,
//        MovieDataContract.MovieDetails.COLUMN_NAME_MOVIE_ID,
//        MovieDataContract.MovieDetails.COLUMN_NAME_TITLE,
//        MovieDataContract.MovieDetails.COLUMN_NAME_POSTER,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RELEASE_DATE,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RATING,
//        MovieDataContract.MovieDetails.COLUMN_NAME_OVERVIEW,
//        MovieDataContract.MovieDetails.COLUMN_NAME_RUNTIME,
//        MovieDataContract.MovieDetails.COLUMN_NAME_VIDEOSW,
//        MovieDataContract.MovieDetails.COLUMN_NAME_USER_REVIEWS
            if (movieDataServer.highestRatedMovies.size() > 0) {
                for (int i = 0; i < highestRatedMovies.size(); i++) {
                    Object[] HighestRatedMoviesRow = new Object[]{
                            movieDataServer.highestRatedMovies.get(i).getId(),
                            movieDataServer.highestRatedMovies.get(i).getMovieId(),
                            movieDataServer.highestRatedMovies.get(i).getTitle(),
                            movieDataServer.highestRatedMovies.get(i).getPoster(),
                            movieDataServer.highestRatedMovies.get(i).getReleaseDate(),
                            movieDataServer.highestRatedMovies.get(i).getRating(),
                            movieDataServer.highestRatedMovies.get(i).getOverview()
                    };
                    matrixCursor.addRow(HighestRatedMoviesRow);
                }
            }
            return matrixCursor;
        }

        private Movie[] parsePopularMoviesJson(String jsonString) {
            Movie[] movieData;
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONArray movies = jsonObject.getJSONArray("results");
                int numMovies = movies.length();
                movieData = new Movie[numMovies];
                for (int i = 0; i < numMovies; i++) {
                    // get JSONObject for a movie and create new Movie based on its data (for each movie in json string)
                    JSONObject movieJsonObject = movies.getJSONObject(i);
                    // Release year from release date
                    String releaseDate = movieJsonObject.getString("release_date");
                    String yearOfRelease = releaseDate.substring(0, releaseDate.indexOf("-"));
                    Movie movie = new Movie(
                            1,
                            movieJsonObject.getString("id"),
                            movieJsonObject.getString("poster_path"),
                            movieJsonObject.getString("title"),
                            movieJsonObject.getString("overview"),
                            movieJsonObject.getString("vote_average"),
                            yearOfRelease);
                    movieData[i] = movie;
                }
            } catch (JSONException e) {
                return null;
            }
            return movieData;
        }

        private String queryTheMovieDbApi(String uriString) {
            InputStream tmdbDataStream = null;
            StringBuffer jsonString = new StringBuffer();
            try {
                URL url = new URL(uriString);
                HttpURLConnection tmdbConnection = (HttpURLConnection) url.openConnection();
                tmdbConnection.setRequestMethod("GET");
                tmdbConnection.connect();
                tmdbDataStream = new BufferedInputStream(tmdbConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(tmdbDataStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonString.append(line + "\n");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tmdbDataStream != null) {
                    try {
                        tmdbDataStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return jsonString.toString();
        }

        public void updatePopularMoviesFromWeb() {
            GetPopularMoviesAsyncTask getPopularMoviesAsyncTask = new GetPopularMoviesAsyncTask();
            getPopularMoviesAsyncTask.execute("https://api.themoviedb.org/3/movie/popular?api_key=" + API_KEY);
        }

        public void updateHighestRatedMoviesFromWeb() {
            GetHighestRatedMoviesAsyncTask getHighestRatedMoviesAsyncTask = new GetHighestRatedMoviesAsyncTask();
            getHighestRatedMoviesAsyncTask.execute("https://api.themoviedb.org/3/movie/top_rated?api_key=" + API_KEY);
        }

        public void updateMovieDetailsFromWeb(String movieId) {
            GetMovieDetailsAsyncTask getMovieDetailsAsyncTask = new GetMovieDetailsAsyncTask();
            getMovieDetailsAsyncTask.execute(movieId);
        }

        public void updateMovieDetailsVideosFromWeb(String movieId) {
            GetMovieDetailsVideosAsyncTask getMovieDetailsVideosAsyncTask = new GetMovieDetailsVideosAsyncTask();
            getMovieDetailsVideosAsyncTask.execute(movieId);
        }

        public void updateMovieDetailsReviewsFromWeb(String movieId) {
            GetMovieDetailsReviewsAsyncTask getMovieDetailsReviewsAsyncTask = new GetMovieDetailsReviewsAsyncTask();
            getMovieDetailsReviewsAsyncTask.execute(movieId);
        }

        private Movie parseMovieDetailsJson(String jsonString) {
            Movie movie = new Movie();
            try {
                JSONObject jsonMovieDetails = new JSONObject(jsonString);
                movie.setMovieId(jsonMovieDetails.getString("id"));
                movie.setTitle(jsonMovieDetails.getString("title"));
                movie.setPoster(jsonMovieDetails.getString("poster_path"));
                movie.setOverview(jsonMovieDetails.getString("overview"));
                movie.setRating(jsonMovieDetails.getString("vote_average"));
                String releaseDate = jsonMovieDetails.getString("release_date");
                String yearOfRelease = releaseDate.substring(0, releaseDate.indexOf("-"));
                movie.setReleaseDate(yearOfRelease);
                movie.setRuntime(jsonMovieDetails.getInt("runtime"));
            } catch (JSONException e) {
                return null;
            }
            return movie;
        }

        private Movie.Video[] parseMovieDetailsVideosJson(String jsonString) {
            Movie movie = new Movie();
            Movie.Video[] videos = null;
            try {
                JSONObject jsonMovieVideos = new JSONObject(jsonString);
                JSONArray resultsVideos = jsonMovieVideos.getJSONArray("results");
                int numVideos = resultsVideos.length();
                videos = new Movie.Video[numVideos];
                for (int i = 0; i < numVideos; i++) {
                    JSONObject video = resultsVideos.getJSONObject(i);
                    videos[i] = movie.new Video(i, video.getString("key"), video.getString("name"), video.getString("site"), video.getString("size"), video.getString("type"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                return null;
            }
            return videos;
        }

        private Movie.UserReview[] parseMovieDetailsReviewsJson(String jsonString) {
            Movie movie = new Movie();
            Movie.UserReview[] reviews = null;
            try {
                JSONObject jsonMovieVideos = new JSONObject(jsonString);
                JSONArray resultsVideos = jsonMovieVideos.getJSONArray("results");
                int numReviews = resultsVideos.length();
                reviews = new Movie.UserReview[numReviews];
                for (int i = 0; i < numReviews; i++) {
                    JSONObject review = resultsVideos.getJSONObject(i);
                    reviews[i] = movie.new UserReview(i, review.getString("author"), review.getString("content"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                return null;
            }
            return reviews;
        }

        public void insertMovieIntoFavorites(String movieId) {
            InsertMovieIntoFavoritesAsyncTask insertMovieIntoFavoritesAsyncTask = new InsertMovieIntoFavoritesAsyncTask();
            insertMovieIntoFavoritesAsyncTask.execute(movieId);
        }

        private class GetPopularMoviesAsyncTask extends AsyncTask<String, Integer, String> {

            @Override
            protected String doInBackground(String... params) {
                return queryTheMovieDbApi(params[0]);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Movie[] uptodatePopularMovies = parsePopularMoviesJson(result);
                // replace old array of popular movies with the fresh one
                popularMovies = new ArrayList(Arrays.asList(uptodatePopularMovies));
                // Notify that content provider's data has changed. Because of that ContentProvider will be requeried
                mContext.getContentResolver().notifyChange(MovieDataContract.PopularMovies.POPULAR_MOVIES_URI, null);
            }
        }

        private class GetHighestRatedMoviesAsyncTask extends AsyncTask<String, Integer, String> {

            @Override
            protected String doInBackground(String... params) {
                return queryTheMovieDbApi(params[0]);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Movie[] uptodateHighestRatedMovies = parsePopularMoviesJson(result);
                // replace old array of popular movies with the fresh one
                highestRatedMovies = new ArrayList(Arrays.asList(uptodateHighestRatedMovies));
                // Notify that content provider's data has changed. Because of that ContentProvider will be requeried
                mContext.getContentResolver().notifyChange(MovieDataContract.HighestRatedMovies.HIGHEST_RATED_MOVIES_URI, null);
            }
        }

        private class GetMovieDetailsAsyncTask extends AsyncTask<String, Integer, String> {

            @Override
            protected String doInBackground(String... params) {
                // https://api.themoviedb.org/3/movie/550?api_key=aef1f8d14e0518ba7f87ee50b62e2392
                String movieDetailsJson = queryTheMovieDbApi("https://api.themoviedb.org/3/movie/" + params[0] + "?api_key=" + API_KEY);
                return movieDetailsJson;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Movie newMovieData = parseMovieDetailsJson(result);
                movieDetails.setId(1);
                movieDetails.setMovieId(newMovieData.getMovieId());
                movieDetails.setTitle(newMovieData.getTitle());
                movieDetails.setPoster(newMovieData.getPoster());
                movieDetails.setOverview(newMovieData.getOverview());
                movieDetails.setRating(newMovieData.getRating());
                movieDetails.setReleaseDate(newMovieData.getReleaseDate());
                movieDetails.setRuntime(newMovieData.getRuntime());
                mContext.getContentResolver().notifyChange(MovieDataContract.MovieDetails.MOVIE_DETAILS_URI, null);
            }
        }

        private class GetMovieDetailsVideosAsyncTask extends AsyncTask<String, Integer, String> {

            @Override
            protected String doInBackground(String... params) {
                // https://api.themoviedb.org/3/movie/550/reviews?api_key=aef1f8d14e0518ba7f87ee50b62e2392
                String movieVideosJson = queryTheMovieDbApi("https://api.themoviedb.org/3/movie/" + params[0] + "/videos?api_key=" + API_KEY);
                return movieVideosJson;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Movie.Video[] videos = parseMovieDetailsVideosJson(result);
                movieDetails.setVideos(videos);
                mContext.getContentResolver().notifyChange(MovieDataContract.MovieDetails.MOVIE_DETAILS_VIDEOS_URI, null);
            }
        }

        private class GetMovieDetailsReviewsAsyncTask extends AsyncTask<String, Integer, String> {

            @Override
            protected String doInBackground(String... params) {
                // https://api.themoviedb.org/3/movie/550/reviews?api_key=aef1f8d14e0518ba7f87ee50b62e2392
                String movieUserReviewsJson = queryTheMovieDbApi("https://api.themoviedb.org/3/movie/" + params[0] + "/reviews?api_key=" + API_KEY);
                return movieUserReviewsJson;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Movie.UserReview[] reviews = parseMovieDetailsReviewsJson(result);
                movieDetails.setUserReviews(reviews);
                mContext.getContentResolver().notifyChange(MovieDataContract.MovieDetails.MOVIE_DETAILS_REVIEWS_URI, null);
            }
        }

        private class InsertMovieIntoFavoritesAsyncTask extends AsyncTask<String, Integer, String[]> {

            @Override
            protected String[] doInBackground(String... params) {
                // https://api.themoviedb.org/3/movie/550?api_key=aef1f8d14e0518ba7f87ee50b62e2392
                String movieDetailsJson = queryTheMovieDbApi("https://api.themoviedb.org/3/movie/" + params[0] + "?api_key=" + API_KEY);
                // https://api.themoviedb.org/3/movie/550/reviews?api_key=aef1f8d14e0518ba7f87ee50b62e2392
                String movieVideosJson = queryTheMovieDbApi("https://api.themoviedb.org/3/movie/" + params[0] + "/videos?api_key=" + API_KEY);
                // https://api.themoviedb.org/3/movie/550/reviews?api_key=aef1f8d14e0518ba7f87ee50b62e2392
                String movieUserReviewsJson = queryTheMovieDbApi("https://api.themoviedb.org/3/movie/" + params[0] + "/reviews?api_key=" + API_KEY);
                return new String[]{movieDetailsJson, movieVideosJson, movieUserReviewsJson};
            }

            @Override
            protected void onPostExecute(String[] result) {
                super.onPostExecute(result);

                SQLiteDatabase db = new MovieDbHelper(getContext()).getWritableDatabase();

                // Parse movie details
                Movie favoritedMovieDetails = parseMovieDetailsJson(result[0]);
                // CREATE TABLE favorite_movies (_id INTEGER PRIMARY KEY, movie_id TEXT NOT NULL UNIQUE, title TEXT NOT NULL, poster TEXT NOT NULL, release_date TEXT NOT NULL, rating TEXT NOT NULL, overview TEXT NOT NULL, runtime INTEGER);
                ContentValues favoritedMovieDetailsValues = new ContentValues();
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_MOVIE_ID, favoritedMovieDetails.getMovieId());
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_TITLE, favoritedMovieDetails.getTitle());
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_POSTER, favoritedMovieDetails.getPoster());
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_RELEASE_DATE, favoritedMovieDetails.getReleaseDate());
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_RATING, favoritedMovieDetails.getRating());
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_OVERVIEW, favoritedMovieDetails.getOverview());
                favoritedMovieDetailsValues.put(MovieDataContract.FavoriteMovies.COLUMN_NAME_RUNTIME, favoritedMovieDetails.getRuntime());
                db.insert(MovieDataContract.FavoriteMovies.TABLE_NAME, null, favoritedMovieDetailsValues);

                //Parse movie videos
                Movie.Video[] favoritedMovieVideos = parseMovieDetailsVideosJson(result[1]);
                //CREATE TABLE videos (_id INTEGER PRIMARY KEY, movie_id TEXT NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL, site TEXT NOT NULL, size INTEGER);
                for (int i = 0; i < favoritedMovieVideos.length; i++) {
                    ContentValues favoritedMovieVideosValues = new ContentValues();
                    favoritedMovieVideosValues.put(MovieDataContract.Videos.COLUMN_NAME_MOVIE_ID, favoritedMovieDetails.getMovieId());
                    favoritedMovieVideosValues.put(MovieDataContract.Videos.COLUMN_NAME_KEY, favoritedMovieVideos[i].getKey());
                    favoritedMovieVideosValues.put(MovieDataContract.Videos.COLUMN_NAME_NAME, favoritedMovieVideos[i].getName());
                    favoritedMovieVideosValues.put(MovieDataContract.Videos.COLUMN_NAME_TYPE, favoritedMovieVideos[i].getType());
                    favoritedMovieVideosValues.put(MovieDataContract.Videos.COLUMN_NAME_SITE, favoritedMovieVideos[i].getSite());
                    favoritedMovieVideosValues.put(MovieDataContract.Videos.COLUMN_NAME_SIZE, favoritedMovieVideos[i].getSize());
                    db.insert(MovieDataContract.Videos.TABLE_NAME, null, favoritedMovieVideosValues);
                }

                // Parse movie reviews
                Movie.UserReview[] favoritedMovieReviews = parseMovieDetailsReviewsJson(result[2]);
                // CREATE TABLE user_reviews (_id INTEGER PRIMARY KEY, movie_id TEXT NOT NULL, author TEXT NOT NULL, content TEXT NOT NULL);
                for (int i = 0; i < favoritedMovieReviews.length; i++) {
                    ContentValues favoritedMovieReviewsValues = new ContentValues();
                    favoritedMovieReviewsValues.put(MovieDataContract.UserReviews.COLUMN_NAME_MOVIE_ID, favoritedMovieDetails.getMovieId());
                    favoritedMovieReviewsValues.put(MovieDataContract.UserReviews.COLUMN_NAME_AUTHOR, favoritedMovieReviews[i].getAuthor());
                    favoritedMovieReviewsValues.put(MovieDataContract.UserReviews.COLUMN_NAME_CONTENT, favoritedMovieReviews[i].getContent());
                    db.insert(MovieDataContract.UserReviews.TABLE_NAME, null, favoritedMovieReviewsValues);
                }

                // Save favorited movie poster into the data/data/com.hugiell.popularmoviesapp/posters/
                String posterUri = "http://image.tmdb.org/t/p/" + MovieDataContract.POSTER_SIZE_500 + favoritedMovieDetails.getPoster();
                Picasso.with(mContext)
                        .load(posterUri)
                        .into(getTarget(favoritedMovieDetails.getPoster()));

                db.close();
            }
        }

    }
}
