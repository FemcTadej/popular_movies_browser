package com.hugiell.popularmoviesapp.data;

import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;

public final class MovieDataContract {

    private MovieDataContract() {
    }

    public static final String POSTER_SIZE_ORIGINAL = "original";
    public static final String POSTER_SIZE_185 = "w185";
    public static final String POSTER_SIZE_500 = "w500";
    public static final String POSTER_SIZE_780 = "w780";

    public static final String FILEPATH_POSTERS = Environment.getDataDirectory() + "/data/" + MovieDataContract.GLOBAL_APP_IDENTIFIER + "/posters";

    public static final String GLOBAL_APP_IDENTIFIER = "com.hugiell.popularmoviesapp";
    public static final String CONTENT_AUTHORITY = "com.hugiell.popularmoviesapp.data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final int CURSOR_LOADER_POPULAR_MOVIES = 0;
    public static final int CURSOR_LOADER_HIGHEST_RATED_MOVIES = 1;
    public static final int CURSOR_LOADER_MOVIE_DETAILS = 2;
    public static final int CURSOR_LOADER_MOVIE_DETAILS_VIDEOS = 3;
    public static final int CURSOR_LOADER_MOVIE_DETAILS_REVIEWS = 4;
    public static final int CURSOR_LOADER_FAVORITE_MOVIES = 5;

    public static class FavoriteMovies implements BaseColumns {
        public static final String TABLE_NAME = "favorite_movies";
        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_OVERVIEW = "overview";
        public static final String COLUMN_NAME_RUNTIME = "runtime";
        public static final String COLUMN_NAME_VIDEOS = "videos";
        public static final String COLUMN_NAME_USER_REVIEWS = "user_reviews";

        public static final Uri FAVORITE_MOVIES_URI = Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(TABLE_NAME).build();

        public static Uri buildFavoriteMoviesUri(String movieId) {
            return FAVORITE_MOVIES_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri buildFavoriteMovieDetailsUri(String movieId) {
            return FAVORITE_MOVIES_URI.buildUpon().appendPath(movieId).build();
        }
    }

    public static class Videos implements BaseColumns {
        public static final String TABLE_NAME = "videos";

        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_SITE = "site";
        public static final String COLUMN_NAME_SIZE = "size";
        public static final String COLUMN_NAME_KEY = "key";

        public static Uri buildFavoriteMovieVideosUri(String movieId) {
            return Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(TABLE_NAME).appendPath(movieId).build();
        }
    }

    public static class UserReviews implements BaseColumns {
        public static final String TABLE_NAME = "user_reviews";

        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_CONTENT = "content";

        public static Uri buildFavoriteMovieUserReviewsUri(String movieId) {
            return Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(TABLE_NAME).appendPath(movieId).build();
        }
    }

    public static class PopularMovies implements BaseColumns {
        public static final String TABLE_NAME = "popular_movies";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_OVERVIEW = "overview";

        public static final Uri POPULAR_MOVIES_URI = Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(TABLE_NAME).build();

        public static final String[] COLUMN_NAMES = {
                MovieDataContract.PopularMovies._ID,
                MovieDataContract.PopularMovies.COLUMN_NAME_MOVIE_ID,
                MovieDataContract.PopularMovies.COLUMN_NAME_TITLE,
                MovieDataContract.PopularMovies.COLUMN_NAME_POSTER,
                MovieDataContract.PopularMovies.COLUMN_NAME_RELEASE_DATE,
                MovieDataContract.PopularMovies.COLUMN_NAME_RATING,
                MovieDataContract.PopularMovies.COLUMN_NAME_OVERVIEW
        };
    }

    public static class HighestRatedMovies implements BaseColumns {
        public static final String TABLE_NAME = "highest_rated_movies";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_OVERVIEW = "overview";

        public static final Uri HIGHEST_RATED_MOVIES_URI = Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(TABLE_NAME).build();

        public static final String[] COLUMN_NAMES = {
                MovieDataContract.PopularMovies._ID,
                MovieDataContract.PopularMovies.COLUMN_NAME_MOVIE_ID,
                MovieDataContract.PopularMovies.COLUMN_NAME_TITLE,
                MovieDataContract.PopularMovies.COLUMN_NAME_POSTER,
                MovieDataContract.PopularMovies.COLUMN_NAME_RELEASE_DATE,
                MovieDataContract.PopularMovies.COLUMN_NAME_RATING,
                MovieDataContract.PopularMovies.COLUMN_NAME_OVERVIEW
        };
    }

    public static class MovieDetails implements BaseColumns {
        public static final String MOVIE_DETAILS = "details";
        public static final String MOVIE_DETAILS_VIDEOS = "details_videos";
        public static final String MOVIE_DETAILS_REVIEWS = "details_reviews";
        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_POSTER = "poster";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_OVERVIEW = "overview";
        public static final String COLUMN_NAME_RUNTIME = "runtime";
        public static final String COLUMN_NAME_VIDEOSW = "videos";
        public static final String COLUMN_NAME_USER_REVIEWS = "user_reviews";
        public static final String COLUMN_NAME_VIDEO_NAME = "video_name";
        public static final String COLUMN_NAME_VIDEO_TYPE = "video_type";
        public static final String COLUMN_NAME_VIDEO_SITE = "video_site";
        public static final String COLUMN_NAME_VIDEO_SIZE = "video_size";
        public static final String COLUMN_NAME_VIDEO_KEY = "video_key";
        public static final String COLUMN_NAME_REVIEW_AUTHOR = "review_author";
        public static final String COLUMN_NAME_REVIEW_CONTENT = "review_content";

        public static final Uri MOVIE_DETAILS_URI = Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(MOVIE_DETAILS).build();
        public static final Uri MOVIE_DETAILS_VIDEOS_URI = Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(MOVIE_DETAILS_VIDEOS).build();
        public static final Uri MOVIE_DETAILS_REVIEWS_URI = Uri.parse("content://" + CONTENT_AUTHORITY).buildUpon().appendPath(MOVIE_DETAILS_REVIEWS).build();

        public static Uri buildMovieDetailsUri(String movieId) {
            return MOVIE_DETAILS_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri buildMovieDetailsVideosUri(String movieId) {
            return MOVIE_DETAILS_VIDEOS_URI.buildUpon().appendPath(movieId).build();
        }

        public static Uri buildMovieDetailsReviewsUri(String movieId) {
            return MOVIE_DETAILS_REVIEWS_URI.buildUpon().appendPath(movieId).build();
        }

        public static final String[] MOVIE_DETAILS_COLUMN_NAMES = {
                MovieDataContract.MovieDetails._ID,
                MovieDataContract.MovieDetails.COLUMN_NAME_MOVIE_ID,
                MovieDataContract.MovieDetails.COLUMN_NAME_TITLE,
                MovieDataContract.MovieDetails.COLUMN_NAME_POSTER,
                MovieDataContract.MovieDetails.COLUMN_NAME_RELEASE_DATE,
                MovieDataContract.MovieDetails.COLUMN_NAME_RATING,
                MovieDataContract.MovieDetails.COLUMN_NAME_OVERVIEW,
                MovieDataContract.MovieDetails.COLUMN_NAME_RUNTIME,
        };
        public static final String[] MOVIE_DETAILS_VIDEOS_COLUMN_NAMES = {
                MovieDataContract.MovieDetails._ID,
                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_NAME,
                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_TYPE,
                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_KEY,
                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_SITE,
                MovieDataContract.MovieDetails.COLUMN_NAME_VIDEO_SIZE
        };

        public static final String[] MOVIE_DETAILS_REVIEWS_COLUMN_NAMES = {
                MovieDataContract.MovieDetails._ID,
                MovieDataContract.MovieDetails.COLUMN_NAME_REVIEW_AUTHOR,
                MovieDataContract.MovieDetails.COLUMN_NAME_REVIEW_CONTENT
        };

    }

}
