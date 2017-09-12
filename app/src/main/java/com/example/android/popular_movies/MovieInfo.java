package com.example.android.popular_movies;

import java.util.List;

/**
 * Created by lamvdoan on 9/5/17.
 */

public class MovieInfo {
    private static String IMAGE_URL = "https://image.tmdb.org/t/p/w185/";

    private int id;
    private String posterPath;
    private String originalTitle;
    private String overview;
    private double voteAverage;
    private String releaseDate;
    private int runtime;
    private List<String> review;

    public List<String> getReview() {
        return review;
    }

    public void setReview(List<String> review) {
        this.review = review;
    }

    public MovieInfo() {
    }

    public MovieInfo(int id, String posterPath) {
        this.id = id;
        setPosterPath(posterPath);
    }

    public int getId() {
        return id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = IMAGE_URL + posterPath;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }
}