package com.hugiell.popularmoviesapp;

public class Movie {

    private int _id;
    private String movieId;
    private String title;
    private String poster;
    private String overview;
    private String rating;
    private String releaseDate;
    private int runtime;
    private Video[] videos;
    private UserReview[] userReviews;

    public Movie() {
    }

    public Movie(int _id, String movieId, String poster, String title, String overview, String rating, String releaseDate) {
        this._id = _id;
        this.movieId = movieId;
        this.title = title;
        this.poster = poster;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public Movie(int _id, String movieId, String poster, String title, String overview, String rating, String releaseDate, short runtime, Video[] videos, UserReview[] userReviews) {
        this._id = _id;
        this.movieId = movieId;
        this.title = title;
        this.poster = poster;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.videos = videos;
        this.userReviews = userReviews;
    }

    public int getId() {
        return _id;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getPoster() {
        return poster;
    }

    public String getOverview() {
        return overview;
    }

    public String getRating() {
        return rating;
    }

    public void setId(int _id) {
        this._id = _id;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public Object[] getMovieAsArray() {
        return new Object[]{this._id, this.movieId, this.title, this.poster, this.releaseDate, this.rating, this.overview};
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public UserReview[] getUserReviews() {
        return userReviews;
    }

    public void setUserReviews(UserReview[] userReviews) {
        this.userReviews = userReviews;
    }

    public Video[] getVideos() {
        return videos;
    }

    public void setVideos(Video[] videos) {
        this.videos = videos;
    }

    public class Video {
        private int _id;
        private String name;
        private String type;
        private String key;
        private String site;
        private String size;

        public Video() {
        }

        public Video(int _id, String key, String name, String site, String size, String type) {
            this._id = _id;
            this.key = key;
            this.name = name;
            this.site = site;
            this.size = size;
            this.type = type;
        }

        public int getId() {
            return _id;
        }

        public void setId(int _id) {
            this._id = _id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public class UserReview {
        private int _id;
        private String author;
        private String content;

        public int getId() {
            return _id;
        }

        public void setId(int _id) {
            this._id = _id;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public UserReview(int _id, String author, String content) {
            this._id = _id;
            this.author = author;
            this.content = content;
        }
    }

}